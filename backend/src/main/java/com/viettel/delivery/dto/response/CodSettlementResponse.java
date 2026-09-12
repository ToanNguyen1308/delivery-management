package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "CodSettlementResponse", description = "Khoan COD can doi soat")
public class CodSettlementResponse implements Serializable {

    private Long id;
    private Long orderId;
    private String orderCode;
    private String receiverName;
    private Long shipperId;
    private String shipperCode;
    private String shipperName;
    private BigDecimal amount;
    private EnumResponse status;
    private LocalDateTime collectedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime confirmedAt;
    private String note;
}
