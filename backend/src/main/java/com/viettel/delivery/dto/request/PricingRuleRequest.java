package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "PricingRuleRequest", description = "Cau hinh bang phi cho mot loai dich vu")
public class PricingRuleRequest implements Serializable {

    @NotNull(message = "Phí cơ bản không được để trống")
    @DecimalMin(value = "0.0", message = "Phí cơ bản không hợp lệ")
    private BigDecimal baseFee;

    @NotNull(message = "Quãng đường cơ bản không được để trống")
    @DecimalMin(value = "0.0", message = "Quãng đường cơ bản không hợp lệ")
    private BigDecimal baseDistanceKm;

    @NotNull(message = "Phí mỗi km vượt không được để trống")
    @DecimalMin(value = "0.0", message = "Phí mỗi km không hợp lệ")
    private BigDecimal perKmFee;

    @NotNull(message = "Khối lượng cơ bản không được để trống")
    @DecimalMin(value = "0.0", message = "Khối lượng cơ bản không hợp lệ")
    private BigDecimal baseWeightKg;

    @NotNull(message = "Phí mỗi kg vượt không được để trống")
    @DecimalMin(value = "0.0", message = "Phí mỗi kg không hợp lệ")
    private BigDecimal perKgFee;

    @DecimalMin(value = "0.0", message = "Phụ phí vùng xa không hợp lệ")
    private BigDecimal remoteAreaSurcharge;

    @DecimalMin(value = "0.0", message = "Tỉ lệ phí COD không hợp lệ")
    private BigDecimal codFeePercent;

    @DecimalMin(value = "0.0", message = "Phí tối thiểu không hợp lệ")
    private BigDecimal minFee;

    private Boolean active;
}
