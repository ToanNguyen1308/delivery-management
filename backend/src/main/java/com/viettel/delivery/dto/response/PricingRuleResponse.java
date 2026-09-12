package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "PricingRuleResponse", description = "Bang phi theo loai dich vu")
public class PricingRuleResponse implements Serializable {

    private Long id;
    private EnumResponse serviceType;
    private BigDecimal baseFee;
    private BigDecimal baseDistanceKm;
    private BigDecimal perKmFee;
    private BigDecimal baseWeightKg;
    private BigDecimal perKgFee;
    private BigDecimal remoteAreaSurcharge;
    private BigDecimal codFeePercent;
    private BigDecimal minFee;
    private Boolean active;
}
