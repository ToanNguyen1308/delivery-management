package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(name = "ShipperLocationResponse", description = "Vi tri shipper day realtime qua WebSocket")
public class ShipperLocationResponse implements Serializable {

    private Long shipperId;
    private String shipperCode;
    private String shipperName;
    private Long orderId;
    private String orderCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private LocalDateTime recordedAt;
}
