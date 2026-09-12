package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "FeePreviewResponse", description = "Chi tiet cuoc van chuyen")
public class FeePreviewResponse implements Serializable {

    private EnumResponse serviceType;
    private BigDecimal distanceKm;
    private BigDecimal baseFee;
    private BigDecimal distanceFee;
    private BigDecimal weightFee;
    private BigDecimal surcharge;

    @Schema(description = "Phi thu ho COD")
    private BigDecimal codFee;

    @Schema(description = "Tong cuoc van chuyen truoc giam gia")
    private BigDecimal shippingFee;

    private BigDecimal discountAmount;

    @Schema(description = "So tien khach phai tra = cuoc van chuyen - giam gia")
    private BigDecimal totalAmount;

    private String voucherCode;
    private boolean voucherApplied;

    @Schema(description = "Ly do voucher khong ap dung duoc")
    private String voucherMessage;
}
