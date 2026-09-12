package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "ShipperResponse", description = "Thong tin shipper")
public class ShipperResponse implements Serializable {

    private Long id;
    private String shipperCode;
    private Long userId;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private EnumResponse vehicleType;
    private String licensePlate;
    private BigDecimal maxLoadKg;
    private String zone;
    private EnumResponse status;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private LocalDateTime lastLocationAt;
    private BigDecimal rating;
    private Integer totalDelivered;
    private Integer totalFailed;
    private Integer currentLoad;
    private Integer maxConcurrentOrders;
    private String idCardUrl;
    private String driverLicenseUrl;
    private String note;
    private LocalDateTime createdDate;
}
