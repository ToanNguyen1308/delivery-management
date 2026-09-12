package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.request.OrderItemRequest;
import com.viettel.delivery.dto.request.OrderUpdateRequest;
import com.viettel.delivery.dto.response.OrderItemResponse;
import com.viettel.delivery.dto.response.OrderResponse;
import com.viettel.delivery.dto.response.OrderStatusHistoryResponse;
import com.viettel.delivery.dto.response.OrderSummaryResponse;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.OrderItem;
import com.viettel.delivery.entity.OrderStatusHistory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerPhone", source = "customer.phoneNumber")
    @Mapping(target = "shipperId", source = "currentShipper.id")
    @Mapping(target = "shipperCode", source = "currentShipper.shipperCode")
    @Mapping(target = "shipperName", source = "currentShipper.user.fullName")
    @Mapping(target = "shipperPhone", source = "currentShipper.user.phoneNumber")
    @Mapping(target = "nextStatuses", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "shipperName", source = "currentShipper.user.fullName")
    OrderSummaryResponse toSummary(Order order);

    List<OrderSummaryResponse> toSummaryList(List<Order> orders);

    OrderItemResponse toItemResponse(OrderItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toItemEntity(OrderItemRequest request);

    OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    List<OrderStatusHistoryResponse> toHistoryResponseList(List<OrderStatusHistory> histories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderCode", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "voucher", ignore = true)
    void updateEntity(@MappingTarget Order order, OrderUpdateRequest request);
}
