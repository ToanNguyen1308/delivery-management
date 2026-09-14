package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "ResetPasswordRequest", description = "Đặt lại mật khẩu")
public class ResetPasswordRequest implements Serializable {

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu mới từ 6 đến 100 ký tự")
    private String newPassword;
}
