package com.viettel.delivery.config;

import com.viettel.delivery.config.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }

    /**
     * Tao bucket neu chua ton tai. Loi ket noi chi ghi log de ung dung van khoi dong duoc
     * khi chay o moi truong khong co MinIO.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucketExists() {
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.bucket())
                    .build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.bucket()).build());
                log.info("Da tao bucket MinIO: {}", minioProperties.bucket());
            } else {
                log.info("Bucket MinIO da san sang: {}", minioProperties.bucket());
            }
        } catch (Exception ex) {
            log.warn("Khong the khoi tao bucket MinIO ({}): {}", minioProperties.endpoint(), ex.getMessage());
        }
    }
}
