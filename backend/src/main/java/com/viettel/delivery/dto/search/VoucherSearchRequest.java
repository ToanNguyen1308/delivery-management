package com.viettel.delivery.dto.search;

import com.viettel.delivery.constant.enums.DiscountType;
import com.viettel.delivery.constant.enums.VoucherStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "VoucherSearchRequest", description = "Dieu kien tim kiem voucher")
public class VoucherSearchRequest extends BaseSearchRequest {

    @Schema(description = "Tim theo ma hoac ten chuong trinh")
    private String keyword;

    private VoucherStatus status;

    private DiscountType discountType;

    @Schema(description = "Chi lay voucher con hieu luc va con luot dung")
    private Boolean availableOnly;
}
