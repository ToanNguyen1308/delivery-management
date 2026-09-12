package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "CodWalletResponse", description = "Vi COD cua shipper")
public class CodWalletResponse implements Serializable {

    private Long shipperId;
    private String shipperCode;

    @Schema(description = "Tien dang giu, chua nop lai cong ty")
    private BigDecimal holdingAmount;

    @Schema(description = "Da nop, cho ke toan xac nhan")
    private BigDecimal submittedAmount;

    @Schema(description = "Da doi soat xong")
    private BigDecimal confirmedAmount;

    private long holdingCount;
    private long submittedCount;
}
