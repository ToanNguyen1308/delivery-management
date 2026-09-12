package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "FeePreviewRequest", description = "Tinh thu cuoc van chuyen truoc khi tao don")
public class FeePreviewRequest implements Serializable {

    @NotNull(message = "Phải chọn loại dịch vụ")
    private ServiceType serviceType;

    @NotNull(message = "Khối lượng không được để trống")
    @DecimalMin(value = "0.1", message = "Khối lượng phải lớn hơn 0")
    private BigDecimal weightKg;

    @Schema(description = "Toa do diem lay hang")
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    @Schema(description = "Toa do diem giao hang")
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    @Schema(description = "Nhap truc tiep quang duong neu khong co toa do", example = "8.5")
    @DecimalMin(value = "0.0", message = "Quãng đường không hợp lệ")
    private BigDecimal distanceKm;

    @DecimalMin(value = "0.0", message = "Tiền thu hộ không hợp lệ")
    private BigDecimal codAmount;

    private Boolean fragile;

    @Schema(description = "Giao vung xa, ap dung phu phi theo bang phi")
    private Boolean remoteArea;

    @Schema(description = "Ma giam gia muon ap dung", example = "FREESHIP")
    private String voucherCode;
}
