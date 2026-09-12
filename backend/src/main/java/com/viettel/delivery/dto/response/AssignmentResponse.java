package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "AssignmentResponse", description = "Thong tin phan cong giao hang")
public class AssignmentResponse implements Serializable {

    private Long id;
    private Long orderId;
    private String orderCode;
    private String receiverName;
    private String receiverPhone;
    private String pickupAddress;
    private String deliveryAddress;
    private BigDecimal codAmount;
    private BigDecimal weightKg;
    private EnumResponse orderStatus;
    private EnumResponse serviceType;

    private Long shipperId;
    private String shipperCode;
    private String shipperName;

    private EnumResponse status;
    private EnumResponse assignType;
    private BigDecimal distanceKm;
    private LocalDateTime assignedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expectedDeliveryAt;
    private String rejectReason;
    private String note;
}
