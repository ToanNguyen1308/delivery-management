package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.response.CodSettlementResponse;
import com.viettel.delivery.entity.CodSettlement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface CodSettlementMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "receiverName", source = "order.receiverName")
    @Mapping(target = "shipperId", source = "shipper.id")
    @Mapping(target = "shipperCode", source = "shipper.shipperCode")
    @Mapping(target = "shipperName", source = "shipper.user.fullName")
    CodSettlementResponse toResponse(CodSettlement settlement);

    List<CodSettlementResponse> toResponseList(List<CodSettlement> settlements);
}
