package fit.iuh.se.hshealthrecord.service.user.impl;

import fit.iuh.se.hshealthrecord.dto.message.RecordProcessingMessage;
import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.event.HealthRecordAnalyzedEvent;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.service.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthRecordServiceImplDirectUploadAndCallbackTest {

    private HealthRecordRepository repository;
    private HealthRecordMapper mapper;
    private S3Service s3Service;
    private RabbitTemplate rabbitTemplate;
    private ApplicationEventPublisher eventPublisher;
    private StringRedisTemplate redisTemplate;
    private TransactionTemplate transactionTemplate;
    private HealthRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(HealthRecordRepository.class);
        mapper = mock(HealthRecordMapper.class);
        s3Service = mock(S3Service.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        redisTemplate = mock(StringRedisTemplate.class);
        transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        service = new HealthRecordServiceImpl(
                repository,
                mapper,
                s3Service,
                rabbitTemplate,
                eventPublisher,
                redisTemplate,
                JsonMapper.builder().build(),
                transactionTemplate
        );
        ReflectionTestUtils.setField(service, "exchange", "health.record.exchange");
        ReflectionTestUtils.setField(service, "routingKey", "health.record.process.routing");
    }

    @Test
    void rejectsNonCsvDirectUploadBeforeTouchingS3OrDb() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", "abc".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.uploadDirectAndProcess(1L, file))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ARGUMENT);

        verify(s3Service, never()).uploadFileDirect(anyString(), any(), anyLong(), anyString());
        verify(transactionTemplate, never()).execute(any());
    }

    @Test
    void uploadsToS3OutsideTransactionThenSavesAndPublishes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ppg_sample.csv", "text/csv", "Time(ms),IR\n0,1000\n".getBytes(StandardCharsets.UTF_8));
        when(s3Service.generateObjectKey(S3Service.FOLDER_RECORDS, 7L, "ppg_sample.csv"))
                .thenReturn("records/7/ppg_sample.csv");
        when(repository.save(any(HealthRecord.class))).thenAnswer(inv -> {
            HealthRecord r = inv.getArgument(0);
            r.setId(900L);
            return r;
        });
        when(mapper.toResponse(any(HealthRecord.class)))
                .thenReturn(HealthRecordResponse.builder().id(900L).status(RecordStatus.PROCESSING).build());

        HealthRecordResponse res = service.uploadDirectAndProcess(7L, file);

        assertThat(res.getId()).isEqualTo(900L);
        InOrder order = inOrder(s3Service, transactionTemplate, repository, rabbitTemplate);
        order.verify(s3Service).uploadFileDirect(eq("records/7/ppg_sample.csv"), any(), anyLong(), eq("text/csv"));
        order.verify(transactionTemplate).execute(any());
        order.verify(repository).save(any(HealthRecord.class));
        order.verify(rabbitTemplate).convertAndSend(eq("health.record.exchange"), eq("health.record.process.routing"), any(RecordProcessingMessage.class));
    }

    @Test
    void updateAiResultIsIdempotentWhenRecordAlreadyCompleted() {
        HealthRecord completed = HealthRecord.builder()
                .id(50L)
                .userId(7L)
                .status(RecordStatus.COMPLETED)
                .predictionLabel(PredictionLabel.NORMAL)
                .build();
        when(repository.findById(50L)).thenReturn(Optional.of(completed));
        when(mapper.toResponse(completed)).thenReturn(HealthRecordResponse.builder().id(50L).build());

        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(50L);
        request.setPredictionLabel(PredictionLabel.AFIB);

        service.updateAiResult(request);

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateAiResultPersistsResultAndPublishesAnalyzedEvent() {
        HealthRecord processing = HealthRecord.builder()
                .id(51L)
                .userId(7L)
                .status(RecordStatus.PROCESSING)
                .build();
        when(repository.findById(51L)).thenReturn(Optional.of(processing));
        when(repository.save(any(HealthRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(HealthRecord.class))).thenReturn(HealthRecordResponse.builder().id(51L).build());
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(51L);
        request.setPredictionLabel(PredictionLabel.AFIB);
        request.setConfidence(0.91);
        request.setAfAlert(true);
        request.setModelVersion("v6.0.0");

        service.updateAiResult(request);

        assertThat(processing.getStatus()).isEqualTo(RecordStatus.COMPLETED);
        assertThat(processing.getPredictionLabel()).isEqualTo(PredictionLabel.AFIB);
        assertThat(processing.getAfAlert()).isTrue();
        assertThat(processing.getModelVersion()).isEqualTo("v6.0.0");
        verify(eventPublisher).publishEvent(any(HealthRecordAnalyzedEvent.class));
    }
}
