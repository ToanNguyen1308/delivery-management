package com.viettel.delivery.entity;

import com.viettel.delivery.constant.enums.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Cau hinh bang phi theo tung loai dich vu.
 */
@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRule extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20, unique = true)
    private ServiceType serviceType;

    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "base_distance_km", nullable = false, precision = 6, scale = 2)
    private BigDecimal baseDistanceKm;

    @Column(name = "per_km_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal perKmFee;

    @Column(name = "base_weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal baseWeightKg;

    @Column(name = "per_kg_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal perKgFee;

    @Builder.Default
    @Column(name = "remote_area_surcharge", precision = 12, scale = 2)
    private BigDecimal remoteAreaSurcharge = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "cod_fee_percent", precision = 5, scale = 2)
    private BigDecimal codFeePercent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "min_fee", precision = 12, scale = 2)
    private BigDecimal minFee = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
