package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "PaymentResultResponse", description = "Ket qua thanh toan tra ve cho frontend")
public class PaymentResultResponse implements Serializable {

    private boolean success;
    private String txnRef;
    private String orderCode;
    private Long orderId;
    private BigDecimal amount;
    private String responseCode;
    private String message;
}
