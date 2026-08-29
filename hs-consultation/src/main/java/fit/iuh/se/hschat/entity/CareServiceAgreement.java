package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "care_service_agreements", indexes = {
        @Index(name = "idx_agreement_request_status", columnList = "request_id, status"),
        @Index(name = "idx_agreement_member_status", columnList = "member_id, status"),
        @Index(name = "idx_agreement_valid_until", columnList = "status, valid_until")
})
public class CareServiceAgreement extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "request_id", updatable = false)
    Long requestId;

    @Column(name = "renewal_id", updatable = false)
    Long renewalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, updatable = false, length = 30)
    @Builder.Default
    CareServiceAgreementType agreementType = CareServiceAgreementType.INITIAL_CARE;

    @Column(name = "member_id", nullable = false, updatable = false)
    Long memberId;

    @Column(name = "doctor_id", nullable = false, updatable = false)
    Long doctorId;

    @Column(name = "package_id", nullable = false, updatable = false)
    Long packageId;

    @Column(name = "package_family_id", nullable = false, updatable = false)
    Long packageFamilyId;

    @Column(name = "package_code", nullable = false, updatable = false, length = 80)
    String packageCode;

    @Column(name = "package_name", nullable = false, updatable = false, length = 160)
    String packageName;

    @Column(name = "package_version", nullable = false, updatable = false)
    Integer packageVersion;

    @Column(name = "service_description", updatable = false, length = 4000)
    String serviceDescription;

    @Builder.Default
    @Immutable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agreement_included_services", joinColumns = @JoinColumn(name = "agreement_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_code", nullable = false, length = 80)
    @OrderColumn(name = "service_order")
    List<CareServiceCode> includedServices = new ArrayList<>();

    @Builder.Default
    @Immutable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agreement_excluded_services", joinColumns = @JoinColumn(name = "agreement_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_code", nullable = false, length = 80)
    @OrderColumn(name = "service_order")
    List<CareServiceCode> excludedServices = new ArrayList<>();

    @Column(name = "price_amount", nullable = false, updatable = false, precision = 14, scale = 2)
    BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, updatable = false, length = 3)
    String currency;

    @Column(name = "duration_days", nullable = false, updatable = false)
    Integer durationDays;

    @Column(name = "extension_starts_at", updatable = false)
    Instant extensionStartsAt;

    @Column(name = "resulting_ends_at", updatable = false)
    Instant resultingEndsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_rule", nullable = false, updatable = false, length = 60)
    CareStartRule startRule;

    @Column(name = "support_schedule_snapshot_json", nullable = false, updatable = false, columnDefinition = "TEXT")
    String supportScheduleSnapshotJson;

    @Column(name = "support_timezone_snapshot", nullable = false, updatable = false, length = 80)
    String supportTimezoneSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_policy", nullable = false, updatable = false, length = 80)
    CareServiceSupportPolicy supportPolicy;

    @Column(name = "renewable", nullable = false, updatable = false)
    Boolean renewable;

    @Column(name = "terms_policy_reference", nullable = false, updatable = false, length = 255)
    String termsPolicyReference;

    @Column(name = "cancellation_policy_reference", nullable = false, updatable = false, length = 255)
    String cancellationPolicyReference;

    @Column(name = "refund_policy_reference", nullable = false, updatable = false, length = 255)
    String refundPolicyReference;

    @Column(name = "emergency_limitation", nullable = false, updatable = false, length = 1000)
    String emergencyLimitation;

    @Column(name = "ai_limitation", nullable = false, updatable = false, length = 1000)
    String aiLimitation;

    @Column(name = "service_limitation", nullable = false, updatable = false, length = 2000)
    String serviceLimitation;

    @Column(name = "health_data_scope_disclosure", nullable = false, updatable = false, length = 2000)
    String healthDataScopeDisclosure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    CareServiceAgreementStatus status;

    @Column(name = "accepted_by_member")
    Long acceptedByMember;

    @Column(name = "accepted_at")
    Instant acceptedAt;

    @Column(name = "valid_until", nullable = false, updatable = false)
    Instant validUntil;

    @Column(name = "invalidated_at")
    Instant invalidatedAt;

    @Column(name = "invalidation_reason", length = 500)
    String invalidationReason;

    @Column(name = "consumed_at")
    Instant consumedAt;

    @Version
    @Column(nullable = false)
    long version;
}
