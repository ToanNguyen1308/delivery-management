package com.viettel.delivery.config.properties;

import com.viettel.delivery.constant.enums.DispatchStrategyType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dispatch")
public record DispatchProperties(DispatchStrategyType strategy, double maxRadiusKm) {

    public DispatchProperties {
        if (strategy == null) {
            strategy = DispatchStrategyType.NEAREST;
        }
        if (maxRadiusKm <= 0) {
            maxRadiusKm = 15;
        }
    }
}
