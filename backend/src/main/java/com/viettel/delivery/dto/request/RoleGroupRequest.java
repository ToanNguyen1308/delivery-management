package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Schema(name = "RoleGroupRequest", description = "Tao hoac cap nhat nhom quyen")
public class RoleGroupRequest implements Serializable {

    @NotBlank(message = "Mã nhóm quyền không được để trống")
    @Size(max = 50, message = "Mã nhóm quyền tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã nhóm quyền chỉ gồm chữ in hoa, số và dấu gạch dưới")
    private String roleGroupCode;

    @NotBlank(message = "Tên nhóm quyền không được để trống")
    @Size(max = 100, message = "Tên nhóm quyền tối đa 100 ký tự")
    private String roleGroupName;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @NotEmpty(message = "Phải chọn ít nhất một chức năng")
    private Set<Long> functionIds;
}
