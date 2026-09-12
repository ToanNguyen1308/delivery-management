package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.response.FunctionResponse;
import com.viettel.delivery.dto.response.RoleGroupResponse;
import com.viettel.delivery.entity.FunctionEntity;
import com.viettel.delivery.entity.RoleGroup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleGroupMapper {

    RoleGroupResponse toResponse(RoleGroup roleGroup);

    List<RoleGroupResponse> toResponseList(List<RoleGroup> roleGroups);

    FunctionResponse toFunctionResponse(FunctionEntity function);

    List<FunctionResponse> toFunctionResponseList(List<FunctionEntity> functions);
}
