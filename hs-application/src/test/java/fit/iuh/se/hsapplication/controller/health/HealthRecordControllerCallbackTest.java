package fit.iuh.se.hsapplication.controller.health;

import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthRecordControllerCallbackTest {

    private HealthRecordService healthRecordService;
    private HealthRecordController controller;

    @BeforeEach
    void setUp() {
        healthRecordService = mock(HealthRecordService.class);
        controller = new HealthRecordController(healthRecordService);
        ReflectionTestUtils.setField(controller, "aiCallbackSecret", "super-secret-key");
        ReflectionTestUtils.setField(controller, "enforceAiCallbackSignature", true);
    }

    @Test
    void acceptsValidHmacCallbackSignature() throws Exception {
        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(555L);
        request.setPredictionLabel(PredictionLabel.NORMAL);
        request.setConfidence(0.12);

        String ts = String.valueOf(Instant.now().getEpochSecond());
        String sig = sign("super-secret-key", ts + ".555.NORMAL");
        HealthRecordResponse expected = HealthRecordResponse.builder().id(555L).build();
        when(healthRecordService.updateAiResult(request)).thenReturn(expected);

        var response = controller.updateAiResult(ts, sig, request);

        assertThat(response.getData()).isEqualTo(expected);
        verify(healthRecordService).updateAiResult(request);
    }

    @Test
    void rejectsInvalidSignatureWhenEnforced() {
        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(555L);
        request.setPredictionLabel(PredictionLabel.AFIB);

        String ts = String.valueOf(Instant.now().getEpochSecond());

        assertThatThrownBy(() -> controller.updateAiResult(ts, "deadbeef", request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void rejectsExpiredTimestampWhenEnforced() throws Exception {
        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(555L);
        request.setPredictionLabel(PredictionLabel.NORMAL);

        String staleTs = String.valueOf(Instant.now().getEpochSecond() - 600L);
        String sig = sign("super-secret-key", staleTs + ".555.NORMAL");

        assertThatThrownBy(() -> controller.updateAiResult(staleTs, sig, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void rejectsEmptySecretWhenEnforced() {
        ReflectionTestUtils.setField(controller, "aiCallbackSecret", "");
        AiCallbackRequest request = new AiCallbackRequest();
        request.setRecordId(555L);
        request.setPredictionLabel(PredictionLabel.NORMAL);

        assertThatThrownBy(() -> controller.updateAiResult(null, null, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    private static String sign(String secret, String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }
}
