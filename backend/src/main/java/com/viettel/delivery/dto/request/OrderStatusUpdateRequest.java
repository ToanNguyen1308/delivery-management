package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "OrderStatusUpdateRequest", description = "Cap nhat trang thai don hang")
public class OrderStatusUpdateRequest implements Serializable {

    @NotNull(message = "Phải chọn trạng thái mới")
    private OrderStatus status;

    @Size(max = 500)
    private String note;

    @Schema(description = "Anh xac nhan giao hang, bat buoc khi chuyen sang DELIVERED")
    private String proofImageUrl;

    @Schema(description = "Ly do khi giao that bai")
    @Size(max = 500)
    private String failureReason;

    @Schema(description = "Toa do noi cap nhat trang thai, dung de ghi tracking")
    private BigDecimal latitude;
    private BigDecimal longitude;
}
