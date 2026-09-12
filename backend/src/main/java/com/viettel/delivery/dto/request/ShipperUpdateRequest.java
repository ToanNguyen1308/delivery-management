package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.constant.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "ShipperUpdateRequest", description = "Cap nhat ho so shipper")
public class ShipperUpdateRequest implements Serializable {

    private VehicleType vehicleType;

    @Size(max = 20, message = "Biển số tối đa 20 ký tự")
    private String licensePlate;

    @DecimalMin(value = "0.1", message = "Tải trọng phải lớn hơn 0")
    private BigDecimal maxLoadKg;

    @Size(max = 100, message = "Khu vực tối đa 100 ký tự")
    private String zone;

    private ShipperStatus status;

    private Integer maxConcurrentOrders;

    private String idCardUrl;

    private String driverLicenseUrl;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
