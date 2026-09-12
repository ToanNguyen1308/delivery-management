package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(name = "OrderResponse", description = "Chi tiet don hang")
public class OrderResponse implements Serializable {

    private Long id;
    private String orderCode;

    private Long customerId;
    private String customerName;
    private String customerPhone;

    private String senderName;
    private String senderPhone;
    private String pickupAddress;
    private String pickupDistrict;
    private String pickupProvince;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private String deliveryDistrict;
    private String deliveryProvince;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    private String packageDescription;
    private BigDecimal weightKg;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private Boolean fragile;

    private EnumResponse serviceType;
    private BigDecimal distanceKm;
    private BigDecimal shippingFee;
    private BigDecimal surcharge;
    private BigDecimal discountAmount;
    private BigDecimal codAmount;
    private BigDecimal totalAmount;
    private String voucherCode;

    private EnumResponse status;
    private EnumResponse paymentMethod;
    private EnumResponse paymentStatus;
    private EnumResponse codSettlementStatus;

    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    private Long shipperId;
    private String shipperCode;
    private String shipperName;
    private String shipperPhone;

    private String proofImageUrl;
    private String cancelReason;
    private String failureReason;
    private String note;

    private LocalDateTime createdDate;
    private String createdBy;

    private List<OrderItemResponse> items;

    @Schema(description = "Cac trang thai hop le co the chuyen tiep tu trang thai hien tai")
    private List<EnumResponse> nextStatuses;
}
