package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Schema(name = "RoleGroupResponse", description = "Nhom quyen kem danh sach chuc nang")
public class RoleGroupResponse implements Serializable {

    private Long id;
    private String roleGroupCode;
    private String roleGroupName;
    private String description;
    private List<FunctionResponse> functions;
}
