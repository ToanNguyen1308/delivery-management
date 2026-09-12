package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "AssignOrderRequest", description = "Phan cong don hang cho shipper")
public class AssignOrderRequest implements Serializable {

    @NotNull(message = "Phải chọn đơn hàng")
    private Long orderId;

    @Schema(description = "Bo trong de he thong tu chon shipper theo chien luoc cau hinh")
    private Long shipperId;

    @Size(max = 500)
    private String note;
}
