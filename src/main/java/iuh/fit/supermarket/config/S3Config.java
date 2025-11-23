package iuh.fit.supermarket.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Cấu hình Cloudflare R2 Storage (tương thích S3 API) cho ứng dụng siêu thị
 */
@Configuration
public class S3Config {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.apiEndpoint}")
    private String apiEndpoint;

    /**
     * Tạo S3Client bean để tương tác với Cloudflare R2
     * 
     * @return S3Client đã được cấu hình để kết nối với R2
     */
    @Bean
    public S3Client amazonS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        // Cấu hình S3 để sử dụng path-style access (bắt buộc cho R2)
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(apiEndpoint))
                .serviceConfiguration(s3Config)
                .build();
    }
}
