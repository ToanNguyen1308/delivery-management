package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.DiscountType;
import com.viettel.delivery.constant.enums.VoucherStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "VoucherRequest", description = "Tao hoac cap nhat ma giam gia")
public class VoucherRequest implements Serializable {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 50, message = "Mã giảm giá tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã giảm giá chỉ gồm chữ in hoa, số và dấu gạch dưới")
    private String code;

    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 150, message = "Tên chương trình tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Phải chọn hình thức giảm giá")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", message = "Giá trị giảm không hợp lệ")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không hợp lệ")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.0", message = "Mức giảm tối đa không hợp lệ")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @Min(value = 1, message = "Giới hạn mỗi người phải lớn hơn 0")
    private Integer usageLimitPerUser;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime validFrom;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime validTo;

    private VoucherStatus status;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
