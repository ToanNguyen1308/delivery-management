package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "OrderItemRequest", description = "Mat hang trong kien")
public class OrderItemRequest implements Serializable {

    @NotBlank(message = "Tên mặt hàng không được để trống")
    @Size(max = 200, message = "Tên mặt hàng tối đa 200 ký tự")
    private String itemName;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Đơn giá không hợp lệ")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Khối lượng không hợp lệ")
    private BigDecimal weightKg;
}
