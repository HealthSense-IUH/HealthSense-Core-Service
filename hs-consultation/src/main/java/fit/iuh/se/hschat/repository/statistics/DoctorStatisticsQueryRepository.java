package fit.iuh.se.hschat.repository.statistics;

import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientConsultationResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientDetailResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientListItemResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientMedicalHistoryResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DoctorStatisticsQueryRepository {

    private static final String CONSULTATION_SUMMARY_SQL = """
            SELECT COUNT(*) AS total_consultations,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to) AS consultations_in_period,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to AND status = 'SCHEDULED') AS scheduled_in_period,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to AND status = 'ACTIVE') AS active_in_period,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to AND status = 'COMPLETED') AS completed_in_period,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to AND status = 'CANCELLED') AS cancelled_in_period
              FROM consultation_sessions
             WHERE doctor_id = :doctorId
            """;

    private static final String PATIENT_SUMMARY_SQL = """
            WITH first_care AS (
                SELECT member_id, MIN(activated_at) AS first_activated_at
                  FROM consultation_sessions
                 WHERE doctor_id = :doctorId AND activated_at IS NOT NULL
                 GROUP BY member_id
            ), period_care AS (
                SELECT DISTINCT member_id
                  FROM consultation_sessions
                 WHERE doctor_id = :doctorId
                   AND activated_at >= :from AND activated_at < :to
            )
            SELECT (SELECT COUNT(*) FROM first_care) AS total_patients,
                   (SELECT COUNT(*) FROM period_care) AS patients_in_period,
                   (SELECT COUNT(*) FROM period_care p JOIN first_care f USING (member_id)
                     WHERE f.first_activated_at >= :from AND f.first_activated_at < :to) AS new_patients_in_period,
                   (SELECT COUNT(*) FROM period_care p JOIN first_care f USING (member_id)
                     WHERE f.first_activated_at < :from) AS returning_patients_in_period,
                   (SELECT COUNT(*) FROM consultation_sessions
                     WHERE doctor_id = :doctorId AND activated_at >= :from AND activated_at < :to) AS consultations_in_period
            """;

    private static final String PATIENT_LIST_FROM_SQL = """
              FROM (
                    SELECT session.member_id,
                           COUNT(*) AS total_consultations,
                           COUNT(*) FILTER (WHERE session.status = 'COMPLETED') AS completed_consultations,
                           MIN(session.activated_at) AS first_consultation_at,
                           MAX(session.activated_at) AS latest_consultation_at,
                           (ARRAY_AGG(session.status ORDER BY session.activated_at DESC, session.id DESC))[1] AS latest_status
                      FROM consultation_sessions session
                     WHERE session.doctor_id = :doctorId
                       AND session.activated_at IS NOT NULL
                     GROUP BY session.member_id
                   ) patient
              JOIN user_accounts account ON account.id = patient.member_id
              JOIN user_profiles profile ON profile.user_id = patient.member_id
             WHERE account.role = 'MEMBER'
               AND EXISTS (
                    SELECT 1 FROM consultation_sessions selected
                     WHERE selected.doctor_id = :doctorId
                       AND selected.member_id = patient.member_id
                       AND selected.activated_at >= :from AND selected.activated_at < :to
                   )
               AND (:keyword = ''
                    OR CAST(patient.member_id AS TEXT) ILIKE :keywordPattern
                    OR profile.display_name ILIKE :keywordPattern)
            """;

    private static final String PATIENT_DETAIL_SQL = """
            SELECT account.id AS member_id, account.email, profile.display_name, profile.phone,
                   profile.date_of_birth, profile.gender, profile.avatar_url, profile.address,
                   profile.timezone, profile.health_insurance_number
              FROM user_accounts account
              JOIN user_profiles profile ON profile.user_id = account.id
             WHERE account.id = :memberId
               AND account.role = 'MEMBER'
               AND EXISTS (
                    SELECT 1 FROM consultation_sessions session
                     WHERE session.doctor_id = :doctorId
                       AND session.member_id = account.id
                       AND session.activated_at IS NOT NULL
                   )
            """;

    private static final String RELATIONSHIP_SQL = """
            SELECT EXISTS (
                SELECT 1 FROM consultation_sessions
                 WHERE doctor_id = :doctorId AND member_id = :memberId AND activated_at IS NOT NULL
            )
            """;

    private static final String CONSULTATION_HISTORY_FROM_SQL = """
              FROM consultation_sessions
             WHERE doctor_id = :doctorId AND member_id = :memberId AND activated_at IS NOT NULL
            """;

    private static final String MEDICAL_HISTORY_FROM_SQL = """
              FROM consultation_final_summaries summary
              JOIN consultation_sessions session ON session.id = summary.session_id
              LEFT JOIN consultation_final_summary_health_records reference ON reference.summary_id = summary.id
             WHERE session.doctor_id = :doctorId
               AND session.member_id = :memberId
               AND session.activated_at IS NOT NULL
               AND summary.status = 'FINALIZED'
             GROUP BY summary.id, summary.session_id, summary.summary, summary.observations,
                      summary.recommendations, summary.follow_up_recommendation, summary.finalized_at
            """;

    private static final String HEALTH_RECORD_FROM_SQL = """
              FROM (
                    SELECT DISTINCT ON (record.id)
                           record.id AS record_id, authz.session_id, record.file_name, record.file_size,
                           record.status, record.prediction_label, record.confidence, record.created_at AS recorded_at,
                           authz.authorization_source, authz.authorized_at
                      FROM consultation_episode_health_records authz
                      JOIN consultation_sessions session ON session.id = authz.session_id
                      JOIN health_records record ON record.id = authz.health_record_id
                     WHERE session.doctor_id = :doctorId
                       AND session.member_id = :memberId
                       AND session.activated_at IS NOT NULL
                       AND record.user_id = :memberId
                     ORDER BY record.id, authz.authorized_at DESC, authz.session_id DESC
                   ) authorized_record
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public DoctorConsultationStatisticsSummary summarizeConsultations(
            Long doctorId, Instant from, Instant to) {
        return jdbc.queryForObject(CONSULTATION_SUMMARY_SQL, rangeParameters(doctorId, from, to),
                (resultSet, rowNumber) -> new DoctorConsultationStatisticsSummary(
                        resultSet.getLong("total_consultations"),
                        resultSet.getLong("consultations_in_period"),
                        resultSet.getLong("scheduled_in_period"),
                        resultSet.getLong("active_in_period"),
                        resultSet.getLong("completed_in_period"),
                        resultSet.getLong("cancelled_in_period")));
    }

    public DoctorPatientStatisticsSummary summarizePatients(Long doctorId, Instant from, Instant to) {
        return jdbc.queryForObject(PATIENT_SUMMARY_SQL, rangeParameters(doctorId, from, to),
                (resultSet, rowNumber) -> new DoctorPatientStatisticsSummary(
                        resultSet.getLong("total_patients"),
                        resultSet.getLong("patients_in_period"),
                        resultSet.getLong("new_patients_in_period"),
                        resultSet.getLong("returning_patients_in_period"),
                        resultSet.getLong("consultations_in_period")));
    }

    public Page<DoctorPatientListItemResponse> findPatients(
            Long doctorId, Instant from, Instant to, String keyword, Pageable pageable) {
        var parameters = rangeParameters(doctorId, from, to)
                .addValue("keyword", keyword)
                .addValue("keywordPattern", "%" + keyword + "%")
                .addValue("limit", pageable.getPageSize(), Types.INTEGER)
                .addValue("offset", pageable.getOffset(), Types.BIGINT);
        String contentSql = """
                SELECT patient.member_id, profile.display_name, profile.avatar_url,
                       patient.total_consultations, patient.completed_consultations,
                       patient.first_consultation_at, patient.latest_consultation_at, patient.latest_status
                """ + PATIENT_LIST_FROM_SQL + """
                 ORDER BY patient.latest_consultation_at DESC, patient.member_id DESC
                 LIMIT :limit OFFSET :offset
                """;
        List<DoctorPatientListItemResponse> content = jdbc.query(contentSql, parameters,
                (resultSet, rowNumber) -> new DoctorPatientListItemResponse(
                        resultSet.getLong("member_id"),
                        resultSet.getString("display_name"),
                        resultSet.getString("avatar_url"),
                        resultSet.getLong("total_consultations"),
                        resultSet.getLong("completed_consultations"),
                        instant(resultSet, "first_consultation_at"),
                        instant(resultSet, "latest_consultation_at"),
                        ConsultationStatus.valueOf(resultSet.getString("latest_status"))));
        Long total = jdbc.queryForObject("SELECT COUNT(*) " + PATIENT_LIST_FROM_SQL, parameters, Long.class);
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    public boolean hasPatientRelationship(Long doctorId, Long memberId) {
        Boolean exists = jdbc.queryForObject(RELATIONSHIP_SQL, identityParameters(doctorId, memberId), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    public Optional<DoctorPatientDetailResponse> findPatientDetail(Long doctorId, Long memberId) {
        return jdbc.query(PATIENT_DETAIL_SQL, identityParameters(doctorId, memberId),
                (resultSet, rowNumber) -> new DoctorPatientDetailResponse(
                        resultSet.getLong("member_id"),
                        resultSet.getString("email"),
                        resultSet.getString("display_name"),
                        resultSet.getString("phone"),
                        resultSet.getObject("date_of_birth", LocalDate.class),
                        resultSet.getString("gender"),
                        resultSet.getString("avatar_url"),
                        resultSet.getString("address"),
                        resultSet.getString("timezone"),
                        resultSet.getString("health_insurance_number")))
                .stream().findFirst();
    }

    public Page<DoctorPatientConsultationResponse> findConsultationHistory(
            Long doctorId, Long memberId, Pageable pageable) {
        var parameters = pageParameters(doctorId, memberId, pageable);
        String sql = """
                SELECT id, status, created_at, started_at, activated_at, ends_at, completed_at, summary_closure_status
                """ + CONSULTATION_HISTORY_FROM_SQL + """
                 ORDER BY activated_at DESC, id DESC LIMIT :limit OFFSET :offset
                """;
        List<DoctorPatientConsultationResponse> content = jdbc.query(sql, parameters,
                (resultSet, rowNumber) -> new DoctorPatientConsultationResponse(
                        resultSet.getLong("id"),
                        ConsultationStatus.valueOf(resultSet.getString("status")),
                        instant(resultSet, "created_at"),
                        instant(resultSet, "started_at"),
                        instant(resultSet, "activated_at"),
                        instant(resultSet, "ends_at"),
                        instant(resultSet, "completed_at"),
                        enumValue(FinalSummaryClosureStatus.class, resultSet.getString("summary_closure_status"))));
        return page(content, pageable, count("SELECT COUNT(*) " + CONSULTATION_HISTORY_FROM_SQL, parameters));
    }

    public Page<DoctorPatientMedicalHistoryResponse> findMedicalHistory(
            Long doctorId, Long memberId, Pageable pageable) {
        var parameters = pageParameters(doctorId, memberId, pageable);
        String sql = """
                SELECT summary.id, summary.session_id, summary.summary, summary.observations,
                       summary.recommendations, summary.follow_up_recommendation, summary.finalized_at,
                       COALESCE(STRING_AGG(CAST(reference.health_record_id AS TEXT), ','
                           ORDER BY reference.health_record_id), '') AS referenced_record_ids
                """ + MEDICAL_HISTORY_FROM_SQL + """
                 ORDER BY summary.finalized_at DESC, summary.id DESC LIMIT :limit OFFSET :offset
                """;
        List<DoctorPatientMedicalHistoryResponse> content = jdbc.query(sql, parameters,
                (resultSet, rowNumber) -> new DoctorPatientMedicalHistoryResponse(
                        resultSet.getLong("id"),
                        resultSet.getLong("session_id"),
                        resultSet.getString("summary"),
                        resultSet.getString("observations"),
                        resultSet.getString("recommendations"),
                        resultSet.getString("follow_up_recommendation"),
                        instant(resultSet, "finalized_at"),
                        parseIds(resultSet.getString("referenced_record_ids"))));
        String countSql = """
                SELECT COUNT(*) FROM consultation_final_summaries summary
                JOIN consultation_sessions session ON session.id = summary.session_id
                WHERE session.doctor_id = :doctorId AND session.member_id = :memberId
                  AND session.activated_at IS NOT NULL AND summary.status = 'FINALIZED'
                """;
        return page(content, pageable, count(countSql, parameters));
    }

    public Page<DoctorPatientHealthRecordResponse> findAuthorizedHealthRecords(
            Long doctorId, Long memberId, Pageable pageable) {
        var parameters = pageParameters(doctorId, memberId, pageable);
        String sql = """
                SELECT record_id, session_id, file_name, file_size, status, prediction_label, confidence,
                       recorded_at, authorization_source, authorized_at
                """ + HEALTH_RECORD_FROM_SQL + """
                 ORDER BY recorded_at DESC, record_id DESC LIMIT :limit OFFSET :offset
                """;
        List<DoctorPatientHealthRecordResponse> content = jdbc.query(sql, parameters,
                (resultSet, rowNumber) -> new DoctorPatientHealthRecordResponse(
                        resultSet.getLong("record_id"),
                        resultSet.getLong("session_id"),
                        resultSet.getString("file_name"),
                        nullableLong(resultSet, "file_size"),
                        RecordStatus.valueOf(resultSet.getString("status")),
                        enumValue(PredictionLabel.class, resultSet.getString("prediction_label")),
                        nullableDouble(resultSet, "confidence"),
                        instant(resultSet, "recorded_at"),
                        EpisodeHealthRecordAuthorizationSource.valueOf(resultSet.getString("authorization_source")),
                        instant(resultSet, "authorized_at")));
        return page(content, pageable,
                count("SELECT COUNT(*) " + HEALTH_RECORD_FROM_SQL, parameters));
    }

    private long count(String sql, MapSqlParameterSource parameters) {
        Long value = jdbc.queryForObject(sql, parameters, Long.class);
        return value == null ? 0 : value;
    }

    private <T> Page<T> page(List<T> content, Pageable pageable, long total) {
        return new PageImpl<>(content, pageable, total);
    }

    private static MapSqlParameterSource rangeParameters(Long doctorId, Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("doctorId", doctorId, Types.BIGINT)
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }

    private static MapSqlParameterSource identityParameters(Long doctorId, Long memberId) {
        return new MapSqlParameterSource()
                .addValue("doctorId", doctorId, Types.BIGINT)
                .addValue("memberId", memberId, Types.BIGINT);
    }

    private static MapSqlParameterSource pageParameters(Long doctorId, Long memberId, Pageable pageable) {
        return identityParameters(doctorId, memberId)
                .addValue("limit", pageable.getPageSize(), Types.INTEGER)
                .addValue("offset", pageable.getOffset(), Types.BIGINT);
    }

    private static Instant instant(ResultSet resultSet, String column) throws SQLException {
        OffsetDateTime value = resultSet.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static Long nullableLong(ResultSet resultSet, String column) throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet resultSet, String column) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    private static Set<Long> parseIds(String value) {
        if (value == null || value.isBlank()) return Set.of();
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isEmpty())
                .map(Long::valueOf).forEach(ids::add);
        return Set.copyOf(ids);
    }
}
