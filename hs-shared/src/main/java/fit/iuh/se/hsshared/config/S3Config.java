package fit.iuh.se.hsshared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Getter
public class S3Config {

    @Value("${aws.s3.bucket:healthsense-data-bucket}")
    private String bucketName;

    @Value("${aws.s3.region:ap-southeast-1}")
    private String region;

    @Value("${aws.access-key-id:test-key}")
    private String accessKey;

    @Value("${aws.secret-access-key:test-secret}")
    private String secretKey;

    @Value("${aws.s3.endpoint-url:}")
    private String endpointUrl;

    @Value("${aws.s3.presigned-url-ttl-minutes:15}")
    private long presignedUrlTtlMinutes;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ));
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ));
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }
        return builder.build();
    }
}
