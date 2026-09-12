package com.viettel.delivery.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long accessExpirationMs, long refreshExpirationMs) {
}
