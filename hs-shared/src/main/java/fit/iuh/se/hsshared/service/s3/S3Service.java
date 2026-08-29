package fit.iuh.se.hsshared.service.s3;

import fit.iuh.se.hsshared.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.io.InputStream;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    public static final String FOLDER_TMP_AVATARS = "tmp/avatars/";
    public static final String FOLDER_AVATARS = "avatars/";
    public static final String FOLDER_RECORDS = "records/";

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Config s3Config;

    /**
     * Sinh Presigned PUT URL để client upload trực tiếp file lên S3
     *
     * @param objectKey Tên file trên S3 (VD: avatars/1/1722000_photo.png)
     * @param contentType Loại nội dung (VD: image/png, text/csv)
     * @return Presigned URL có thời hạn (mặc định 15 phút)
     */
    public String generatePresignedUploadUrl(String objectKey, String contentType) {
        log.info("Generating Presigned Upload URL for key: {} in bucket: {} with contentType: {}", 
                 objectKey, s3Config.getBucketName(), contentType);
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(s3Config.getPresignedUrlTtlMinutes()))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating Presigned Upload URL for key {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo Presigned URL upload S3: " + e.getMessage(), e);
        }
    }

    public String generatePresignedDownloadUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank())
            throw new IllegalArgumentException("Object key must not be blank");
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Config.getPresignedUrlTtlMinutes()))
                .getObjectRequest(objectRequest)
                .build();
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Tạo Object Key chuẩn theo thư mục, ID người dùng và Timestamp
     */
    public String generateObjectKey(String folder, Long userId, String fileName) {
        String cleanName = fileName != null ? fileName.trim().replaceAll("[^a-zA-Z0-9.\\-_]", "_").toLowerCase() : "file";
        String normalizedFolder = folder.endsWith("/") ? folder : folder + "/";
        return String.format("%s%d/%d_%s", normalizedFolder, userId, System.currentTimeMillis(), cleanName);
    }

    /**
     * Chuyển đổi Object Key thành Public URL (dành cho các thư mục Public Read như avatars/)
     */
    public String getPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return null;
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) return objectKey;
        if (s3Config.getEndpointUrl() != null && !s3Config.getEndpointUrl().isBlank()) {
            String baseUrl = s3Config.getEndpointUrl().endsWith("/") 
                    ? s3Config.getEndpointUrl().substring(0, s3Config.getEndpointUrl().length() - 1) 
                    : s3Config.getEndpointUrl();
            return String.format("%s/%s/%s", baseUrl, s3Config.getBucketName(), objectKey);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                s3Config.getBucketName(), s3Config.getRegion(), objectKey);
    }

    /**
     * Trích xuất S3 Object Key từ một URL bất kỳ (hoặc giữ nguyên nếu đã là key)
     */
    public String extractObjectKeyFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url; // Đã là objectKey (ví dụ: avatars/1/xxx.png)
        }
        if (s3Config.getEndpointUrl() != null && !s3Config.getEndpointUrl().isBlank()) {
            String baseUrl = s3Config.getEndpointUrl().endsWith("/") 
                    ? s3Config.getEndpointUrl() : s3Config.getEndpointUrl() + "/";
            String customPrefix = baseUrl + s3Config.getBucketName() + "/";
            if (url.startsWith(customPrefix)) {
                return url.substring(customPrefix.length());
            }
        }
        String publicPrefix = String.format("https://%s.s3.%s.amazonaws.com/", 
                s3Config.getBucketName(), s3Config.getRegion());
        if (url.startsWith(publicPrefix)) {
            return url.substring(publicPrefix.length());
        }
        int idxTmpAvatars = url.indexOf(FOLDER_TMP_AVATARS);
        if (idxTmpAvatars != -1) return url.substring(idxTmpAvatars);
        int idxAvatars = url.indexOf(FOLDER_AVATARS);
        if (idxAvatars != -1) return url.substring(idxAvatars);
        int idxRecords = url.indexOf(FOLDER_RECORDS);
        if (idxRecords != -1) return url.substring(idxRecords);
        int idxTmp = url.indexOf("tmp/");
        if (idxTmp != -1) return url.substring(idxTmp);
        
        return null; // Không thuộc S3 bucket của dự án (ví dụ: ảnh từ Google/Facebook login)
    }

    /**
     * Di chuyển object từ đường dẫn tạm sang đường dẫn chính thức (Copy -> Delete)
     *
     * @param sourceKey Đường dẫn source (VD: tmp/avatars/1/xxx.png)
     * @param destinationKey Đường dẫn đích (VD: avatars/1/xxx.png)
     * @return Public URL của object mới sau khi di chuyển
     */
    public String moveFile(String sourceKey, String destinationKey) {
        if (sourceKey == null || sourceKey.isBlank() || destinationKey == null || destinationKey.isBlank()) {
            throw new IllegalArgumentException("Source key and destination key must not be blank");
        }
        log.info("Moving S3 object from key: {} to key: {} in bucket: {}", sourceKey, destinationKey, s3Config.getBucketName());
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(s3Config.getBucketName())
                    .sourceKey(sourceKey)
                    .destinationBucket(s3Config.getBucketName())
                    .destinationKey(destinationKey)
                    .build());
            log.info("Successfully copied object to {}", destinationKey);

            deleteFile(sourceKey);
            return getPublicUrl(destinationKey);
        } catch (Exception e) {
            log.error("Failed to move S3 object from {} to {}: {}", sourceKey, destinationKey, e.getMessage(), e);
            throw new RuntimeException("Không thể di chuyển file trên hệ thống lưu trữ S3: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa một file khỏi AWS S3 (dùng khi cập nhật avatar mới thì xóa avatar cũ)
     */
    public void deleteFile(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        log.info("Attempting to delete file from S3 bucket: {}, key: {}", s3Config.getBucketName(), objectKey);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build());
            log.info("Successfully deleted file from S3: {}", objectKey);
        } catch (Exception e) {
            log.warn("Failed to delete file from S3 (key: {}): {}", objectKey, e.getMessage());
            // Không ném ngoại lệ để tránh rollback transaction chính trong Database
        }
    }

    public String getBucketName() {
        return s3Config.getBucketName();
    }

    /**
     * Upload trực tiếp InputStream từ Backend lên S3 / MinIO (dành cho API tiện ích 1-bước uploadDirect)
     */
    public String uploadFileDirect(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        log.info("Direct uploading file to S3 key: {} in bucket: {} (size: {} bytes)", objectKey, s3Config.getBucketName(), contentLength);
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Successfully direct uploaded file to S3: {}", objectKey);
            return getPublicUrl(objectKey);
        } catch (Exception e) {
            log.error("Failed direct upload to S3 for key {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Không thể upload trực tiếp lên S3: " + e.getMessage(), e);
        }
    }
}
