package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "ShipperCreateRequest", description = "Tao ho so shipper cho mot tai khoan da co")
public class ShipperCreateRequest implements Serializable {

    @NotNull(message = "Phải chọn tài khoản người dùng")
    private Long userId;

    @NotNull(message = "Phải chọn loại phương tiện")
    private VehicleType vehicleType;

    @Size(max = 20, message = "Biển số tối đa 20 ký tự")
    private String licensePlate;

    @DecimalMin(value = "0.1", message = "Tải trọng phải lớn hơn 0")
    private BigDecimal maxLoadKg;

    @Size(max = 100, message = "Khu vực tối đa 100 ký tự")
    private String zone;

    @Schema(description = "So don toi da co the nhan cung luc", example = "5")
    private Integer maxConcurrentOrders;

    private String idCardUrl;

    private String driverLicenseUrl;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
