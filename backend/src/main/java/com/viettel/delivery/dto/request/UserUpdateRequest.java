package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Schema(name = "UserUpdateRequest", description = "Cap nhat nguoi dung")
public class UserUpdateRequest implements Serializable {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;

    @Size(max = 20, message = "Số CCCD tối đa 20 ký tự")
    private String identityNumber;

    private LocalDate birthday;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    private String avatarUrl;

    private UserStatus status;

    @Schema(description = "Danh sach id nhom quyen, bo trong neu khong doi")
    private Set<Long> roleGroupIds;
}
