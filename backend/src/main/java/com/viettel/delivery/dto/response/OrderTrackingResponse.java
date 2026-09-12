package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(name = "OrderTrackingResponse", description = "Thong tin theo doi hanh trinh don hang")
public class OrderTrackingResponse implements Serializable {

    private Long orderId;
    private String orderCode;
    private EnumResponse status;
    private EnumResponse serviceType;

    @Schema(description = "Ten nguoi nhan da che bot ky tu khi tra cuu cong khai")
    private String receiverName;

    private String deliveryAddress;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    private String shipperName;
    private String shipperPhone;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private LocalDateTime lastLocationAt;

    private String proofImageUrl;

    private List<TrackingEventResponse> events;

    @Schema(description = "Cac diem GPS shipper da di qua, dung de ve duong di tren ban do")
    private List<RoutePoint> route;

    @Getter
    @Builder
    @Schema(name = "RoutePoint", description = "Mot diem toa do tren hanh trinh")
    public static class RoutePoint implements Serializable {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private LocalDateTime recordedAt;
    }
}
