package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "CreatePaymentRequest", description = "Khoi tao giao dich thanh toan online")
public class CreatePaymentRequest implements Serializable {

    @NotNull(message = "Phải chọn đơn hàng cần thanh toán")
    private Long orderId;

    @Schema(description = "Ma ngan hang muon thanh toan truc tiep, bo trong de chon tren cong VNPay",
            example = "NCB")
    private String bankCode;
}
