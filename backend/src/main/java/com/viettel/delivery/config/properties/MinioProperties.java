package com.viettel.delivery.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(String endpoint,
                              String publicEndpoint,
                              String accessKey,
                              String secretKey,
                              String bucket) {
}
