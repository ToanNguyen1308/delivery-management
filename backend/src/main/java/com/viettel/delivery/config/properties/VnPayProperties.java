package com.viettel.delivery.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vnpay")
public record VnPayProperties(boolean enabled,
                              String tmnCode,
                              String hashSecret,
                              String payUrl,
                              String apiUrl,
                              String returnUrl,
                              String version,
                              String command,
                              String currencyCode,
                              String locale) {

    /**
     * Chi coi la da cau hinh khi co du TmnCode va HashSecret tu bien moi truong.
     */
    public boolean isConfigured() {
        return enabled
                && tmnCode != null && !tmnCode.isBlank()
                && hashSecret != null && !hashSecret.isBlank();
    }
}
