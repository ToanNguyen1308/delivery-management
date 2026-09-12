package com.viettel.delivery.entity;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.constant.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shippers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Shipper extends BaseEntity {

    @Column(name = "shipper_code", nullable = false, length = 20, unique = true)
    private String shipperCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "max_load_kg", precision = 10, scale = 2)
    private BigDecimal maxLoadKg;

    @Column(name = "zone", length = 100)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShipperStatus status = ShipperStatus.OFFLINE;

    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @Column(name = "last_location_at")
    private LocalDateTime lastLocationAt;

    @Builder.Default
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Builder.Default
    @Column(name = "total_delivered")
    private Integer totalDelivered = 0;

    @Builder.Default
    @Column(name = "total_failed")
    private Integer totalFailed = 0;

    @Builder.Default
    @Column(name = "current_load")
    private Integer currentLoad = 0;

    @Builder.Default
    @Column(name = "max_concurrent_orders")
    private Integer maxConcurrentOrders = 5;

    @Column(name = "id_card_url", length = 500)
    private String idCardUrl;

    @Column(name = "driver_license_url", length = 500)
    private String driverLicenseUrl;

    @Column(name = "note", length = 500)
    private String note;

    public boolean hasCapacity() {
        return currentLoad == null || maxConcurrentOrders == null || currentLoad < maxConcurrentOrders;
    }

    public boolean hasLocation() {
        return currentLatitude != null && currentLongitude != null;
    }

    public void increaseLoad() {
        this.currentLoad = (currentLoad == null ? 0 : currentLoad) + 1;
        if (ShipperStatus.ONLINE.equals(status)) {
            this.status = ShipperStatus.BUSY;
        }
    }

    public void decreaseLoad() {
        this.currentLoad = Math.max(0, (currentLoad == null ? 0 : currentLoad) - 1);
        if (this.currentLoad == 0 && ShipperStatus.BUSY.equals(status)) {
            this.status = ShipperStatus.ONLINE;
        }
    }
}
