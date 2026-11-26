package fit.iuh.se.hsshared.service.s3;

import fit.iuh.se.hsshared.config.S3Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class S3ServiceTest {

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "bucketName", "hs-bucket");
        ReflectionTestUtils.setField(config, "region", "ap-southeast-1");
        ReflectionTestUtils.setField(config, "endpointUrl", "https://minio.local");
        s3Service = new S3Service(mock(S3Presigner.class), mock(S3Client.class), config);
    }

    @Test
    void sanitizesPathTraversalAndSpecialCharsInObjectKey() {
        String key = s3Service.generateObjectKey(S3Service.FOLDER_RECORDS, 42L, "../../evil file (1).CSV");

        assertThat(key).startsWith("records/42/");
        assertThat(key).doesNotContain("..");
        assertThat(key).endsWith("evil_file__1_.csv");
    }

    @Test
    void buildsAndExtractsCustomEndpointPublicUrl() {
        String url = s3Service.getPublicUrl("avatars/42/photo.png");
        assertThat(url).isEqualTo("https://minio.local/hs-bucket/avatars/42/photo.png");
        assertThat(s3Service.extractObjectKeyFromUrl(url)).isEqualTo("avatars/42/photo.png");
    }
}
