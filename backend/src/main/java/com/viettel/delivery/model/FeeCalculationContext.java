package com.viettel.delivery.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Du lieu dau vao de tinh cuoc van chuyen.
 */
public record FeeCalculationContext(BigDecimal distanceKm,
                                    BigDecimal weightKg,
                                    BigDecimal codAmount,
                                    boolean fragile,
                                    boolean remoteArea,
                                    LocalDateTime orderTime) {

    public FeeCalculationContext {
        distanceKm = distanceKm == null ? BigDecimal.ZERO : distanceKm;
        weightKg = weightKg == null ? BigDecimal.ZERO : weightKg;
        codAmount = codAmount == null ? BigDecimal.ZERO : codAmount;
        orderTime = orderTime == null ? LocalDateTime.now() : orderTime;
    }
}
