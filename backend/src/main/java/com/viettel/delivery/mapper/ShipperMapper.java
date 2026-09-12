package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.request.ShipperUpdateRequest;
import com.viettel.delivery.dto.response.ShipperResponse;
import com.viettel.delivery.entity.Shipper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface ShipperMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    ShipperResponse toResponse(Shipper shipper);

    List<ShipperResponse> toResponseList(List<Shipper> shippers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shipperCode", ignore = true)
    @Mapping(target = "currentLoad", ignore = true)
    @Mapping(target = "totalDelivered", ignore = true)
    @Mapping(target = "totalFailed", ignore = true)
    @Mapping(target = "rating", ignore = true)
    void updateEntity(@MappingTarget Shipper shipper, ShipperUpdateRequest request);
}
