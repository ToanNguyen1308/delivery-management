package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(name = "OrderCreateRequest", description = "Tao don hang moi")
public class OrderCreateRequest implements Serializable {

    /* ---------- Nguoi gui ---------- */
    @NotBlank(message = "Tên người gửi không được để trống")
    @Size(max = 150)
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại người gửi không đúng định dạng")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ lấy hàng không được để trống")
    @Size(max = 500)
    private String pickupAddress;

    @Size(max = 100)
    private String pickupDistrict;

    @Size(max = 100)
    private String pickupProvince;

    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    /* ---------- Nguoi nhan ---------- */
    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 150)
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại người nhận không đúng định dạng")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500)
    private String deliveryAddress;

    @Size(max = 100)
    private String deliveryDistrict;

    @Size(max = 100)
    private String deliveryProvince;

    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    /* ---------- Kien hang ---------- */
    @Size(max = 500)
    private String packageDescription;

    @NotNull(message = "Khối lượng không được để trống")
    @DecimalMin(value = "0.1", message = "Khối lượng phải lớn hơn 0")
    private BigDecimal weightKg;

    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private Boolean fragile;

    @Schema(description = "Giao vung xa, ap dung phu phi theo bang phi")
    private Boolean remoteArea;

    /* ---------- Dich vu va thanh toan ---------- */
    @NotNull(message = "Phải chọn loại dịch vụ")
    private ServiceType serviceType;

    @NotNull(message = "Phải chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    @DecimalMin(value = "0.0", message = "Tiền thu hộ không hợp lệ")
    @Schema(description = "So tien thu ho nguoi nhan (COD)")
    private BigDecimal codAmount;

    @Schema(description = "Ma giam gia", example = "FREESHIP")
    private String voucherCode;

    @Size(max = 500)
    private String note;

    @Valid
    private List<OrderItemRequest> items;
}
