package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "ShipperPerformanceResponse", description = "Hieu suat giao hang cua shipper")
public class ShipperPerformanceResponse implements Serializable {

    private Long shipperId;
    private String shipperCode;
    private String fullName;
    private long totalAssigned;
    private long totalDelivered;
    private long totalFailed;
    private long inProgress;

    @Schema(description = "Ti le giao thanh cong (%)")
    private BigDecimal successRate;

    private BigDecimal rating;
}
