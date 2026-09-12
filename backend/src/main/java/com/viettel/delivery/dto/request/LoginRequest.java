package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "LoginRequest", description = "Thong tin dang nhap")
public class LoginRequest implements Serializable {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(example = "Admin@123")
    private String password;
}
