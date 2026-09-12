package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.response.AssignmentResponse;
import com.viettel.delivery.entity.DeliveryAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface AssignmentMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "receiverName", source = "order.receiverName")
    @Mapping(target = "receiverPhone", source = "order.receiverPhone")
    @Mapping(target = "pickupAddress", source = "order.pickupAddress")
    @Mapping(target = "deliveryAddress", source = "order.deliveryAddress")
    @Mapping(target = "codAmount", source = "order.codAmount")
    @Mapping(target = "weightKg", source = "order.weightKg")
    @Mapping(target = "orderStatus", source = "order.status")
    @Mapping(target = "serviceType", source = "order.serviceType")
    @Mapping(target = "expectedDeliveryAt", source = "order.expectedDeliveryAt")
    @Mapping(target = "shipperId", source = "shipper.id")
    @Mapping(target = "shipperCode", source = "shipper.shipperCode")
    @Mapping(target = "shipperName", source = "shipper.user.fullName")
    AssignmentResponse toResponse(DeliveryAssignment assignment);

    List<AssignmentResponse> toResponseList(List<DeliveryAssignment> assignments);
}
