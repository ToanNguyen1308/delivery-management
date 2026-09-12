package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "OrderUpdateRequest", description = "Cap nhat don hang khi con o trang thai cho phep sua")
public class OrderUpdateRequest implements Serializable {

    @Size(max = 150)
    private String receiverName;

    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại người nhận không đúng định dạng")
    private String receiverPhone;

    @Size(max = 500)
    private String deliveryAddress;

    @Size(max = 100)
    private String deliveryDistrict;

    @Size(max = 100)
    private String deliveryProvince;

    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    @Size(max = 500)
    private String packageDescription;

    @Size(max = 500)
    private String note;
}
