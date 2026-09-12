package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "PaymentInitResponse", description = "Thong tin de chuyen huong sang cong thanh toan")
public class PaymentInitResponse implements Serializable {

    private Long paymentId;
    private String txnRef;
    private String orderCode;
    private BigDecimal amount;

    @Schema(description = "URL chuyen huong nguoi dung sang cong thanh toan")
    private String payUrl;

    @Schema(description = "true khi he thong dang chay o che do gia lap vi chua cau hinh VNPay sandbox")
    private boolean mockMode;
}
