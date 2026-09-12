package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.request.RegisterRequest;
import com.viettel.delivery.dto.request.UserCreateRequest;
import com.viettel.delivery.dto.request.UserUpdateRequest;
import com.viettel.delivery.dto.response.RoleGroupSummaryResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface UserMapper {

    @Mapping(target = "roleGroups", source = "roleGroups")
    UserResponse toResponse(User user);

    RoleGroupSummaryResponse toRoleGroupSummary(RoleGroup roleGroup);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roleGroups", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "identityNumber", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roleGroups", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    User toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roleGroups", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);
}
