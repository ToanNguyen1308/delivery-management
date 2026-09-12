package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "PaymentResponse", description = "Giao dich thanh toan")
public class PaymentResponse implements Serializable {

    private Long id;
    private String txnRef;
    private Long orderId;
    private String orderCode;
    private EnumResponse provider;
    private BigDecimal amount;
    private EnumResponse status;
    private String transactionNo;
    private String bankCode;
    private String cardType;
    private String responseCode;
    private LocalDateTime paidAt;
    private LocalDateTime createdDate;
}
