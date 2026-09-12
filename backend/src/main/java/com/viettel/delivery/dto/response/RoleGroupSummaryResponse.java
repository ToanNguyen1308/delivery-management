package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "RoleGroupSummaryResponse", description = "Thong tin rut gon cua nhom quyen")
public class RoleGroupSummaryResponse implements Serializable {

    private Long id;
    private String roleGroupCode;
    private String roleGroupName;
}
