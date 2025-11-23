package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.service.S3FileUploadService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation cho S3FileUploadService
 */
@Service
@Slf4j
public class S3FileUploadServiceImpl implements S3FileUploadService {

    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    // Public URL để tạo link truy cập file (VD: https://pub-xxx.r2.dev)
    @Value("${aws.publicUrl}")
    private String publicUrl;

    @Value("${product.image.max-size:5MB}")
    private String maxFileSize;

    @Value("${product.image.allowed-types}")
    private String allowedTypes;

    /**
     * Constructor với dependency injection
     */
    public S3FileUploadServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload file lên S3 bucket và trả về URL
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        log.info("Bắt đầu upload file: {} vào thư mục: {}", file.getOriginalFilename(), folder);

        if (!isValidFile(file)) {
            throw new IllegalArgumentException("File không hợp lệ");
        }

        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Upload file
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Tạo URL công khai cho Cloudflare R2
            // Format: https://<endpoint>/<bucket>/<key>
            String fileUrl = generateR2PublicUrl(fileName);

            log.info("Upload file thành công: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("Lỗi khi upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể upload file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi S3 khi upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi S3: " + e.getMessage());
        }
    }

    /**
     * Xóa file từ S3 bucket
     */
    @Override
    public boolean deleteFile(String fileUrl) {
        log.info("Bắt đầu xóa file: {}", fileUrl);

        try {
            // Trích xuất key từ URL
            String key = extractKeyFromUrl(fileUrl);
            if (StringUtils.isEmpty(key)) {
                log.warn("Không thể trích xuất key từ URL: {}", fileUrl);
                return false;
            }

            // Tạo request xóa
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Xóa file
            s3Client.deleteObject(deleteObjectRequest);

            log.info("Xóa file thành công: {}", fileUrl);
            return true;

        } catch (Exception e) {
            log.error("Lỗi khi xóa file: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra file có hợp lệ không
     */
    @Override
    public boolean isValidFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File rỗng hoặc null");
            return false;
        }

        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            log.warn("Content type của file không xác định");
            return false;
        }

        List<String> allowedTypesList = Arrays.asList(allowedTypes.split(","));
        if (contentType != null && !allowedTypesList.contains(contentType.toLowerCase())) {
            log.warn("Định dạng file không được phép: {}", contentType);
            return false;
        }

        // Kiểm tra kích thước file (convert maxFileSize string to bytes)
        long maxSizeBytes = parseMaxFileSize(maxFileSize);
        if (file.getSize() > maxSizeBytes) {
            log.warn("File quá lớn: {} bytes, giới hạn: {} bytes", file.getSize(), maxSizeBytes);
            return false;
        }

        return true;
    }

    /**
     * Tạo tên file duy nhất
     */
    @Override
    public String generateUniqueFileName(String originalFilename) {
        if (StringUtils.isEmpty(originalFilename)) {
            originalFilename = "image";
        }

        // Lấy extension
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // Tạo tên file unique với timestamp và UUID
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s_%s%s", "product_image", timestamp, uniqueId, extension);
    }

    /**
     * Trích xuất key từ S3 URL
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            if (StringUtils.isEmpty(fileUrl)) {
                return null;
            }

            // Format: https://bucket-name.s3.amazonaws.com/key
            // hoặc: https://s3.amazonaws.com/bucket-name/key
            URL url = new URL(fileUrl);
            String path = url.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất key từ URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tạo public URL cho Cloudflare R2
     * 
     * @param key Object key trong bucket
     * @return Public URL để truy cập file
     */
    private String generateR2PublicUrl(String key) {
        // Format: https://<r2-public-endpoint>/<key>
        // Ví dụ: https://pub-xxx.r2.dev/product_image.png
        return String.format("%s/%s", publicUrl, key);
    }

    /**
     * Parse max file size string thành bytes
     */
    private long parseMaxFileSize(String maxSizeStr) {
        if (StringUtils.isEmpty(maxSizeStr)) {
            return 5 * 1024 * 1024; // Default 5MB
        }

        maxSizeStr = maxSizeStr.toUpperCase().trim();

        if (maxSizeStr.endsWith("KB")) {
            return Long.parseLong(maxSizeStr.replace("KB", "")) * 1024;
        } else if (maxSizeStr.endsWith("MB")) {
            return Long.parseLong(maxSizeStr.replace("MB", "")) * 1024 * 1024;
        } else if (maxSizeStr.endsWith("GB")) {
            return Long.parseLong(maxSizeStr.replace("GB", "")) * 1024 * 1024 * 1024;
        } else {
            // Assume bytes
            return Long.parseLong(maxSizeStr);
        }
    }

    /**
     * Upload byte array lên S3 bucket
     */
    @Override
    public String uploadFile(byte[] fileBytes, String fileName, String contentType, String folder) {
        log.info("Bắt đầu upload byte array với tên file: {} vào thư mục: {}", fileName, folder);

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File bytes không được rỗng");
        }

        try {
            // Tạo full path với folder nếu có
            String fullKey = StringUtils.isEmpty(folder) ? fileName : folder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(fullKey)
                    .contentType(contentType)
                    .build();

            // Upload file
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            // Tạo URL công khai cho Cloudflare R2
            String fileUrl = generateR2PublicUrl(fullKey);

            log.info("Upload byte array thành công: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("Lỗi S3 khi upload byte array: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi S3: " + e.getMessage());
        }
    }
}
