package com.viettel.delivery.dto.search;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.constant.enums.VehicleType;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ShipperSearchRequest", description = "Dieu kien tim kiem shipper")
public class ShipperSearchRequest extends BaseSearchRequest {

    @Schema(description = "Tim theo ma shipper, ho ten, so dien thoai hoac bien so")
    private String keyword;

    private ShipperStatus status;

    private VehicleType vehicleType;

    private String zone;

    @Schema(description = "Chi lay shipper con cho trong de nhan don")
    private Boolean availableOnly;
}
