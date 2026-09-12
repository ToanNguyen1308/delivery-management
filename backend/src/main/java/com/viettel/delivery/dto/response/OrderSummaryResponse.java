package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ban rut gon cho man hinh danh sach, chi gom truong duoc hien thi tren bang.
 */
@Getter
@Setter
@Schema(name = "OrderSummaryResponse", description = "Don hang rut gon cho man hinh danh sach")
public class OrderSummaryResponse implements Serializable {

    private Long id;
    private String orderCode;
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private String deliveryProvince;
    private EnumResponse serviceType;
    private EnumResponse status;
    private EnumResponse paymentMethod;
    private EnumResponse paymentStatus;
    private BigDecimal shippingFee;
    private BigDecimal codAmount;
    private BigDecimal totalAmount;
    private String customerName;
    private String shipperName;
    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime createdDate;
}
