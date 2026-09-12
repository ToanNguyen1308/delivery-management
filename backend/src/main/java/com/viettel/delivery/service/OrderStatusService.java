package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.dto.request.OrderStatusUpdateRequest;
import com.viettel.delivery.entity.Order;

/**
 * Quan ly state machine cua don hang: kiem tra chuyen tiep hop le, ghi lich su va phat su kien.
 */
public interface OrderStatusService {

    void changeStatus(Order order, OrderStatusUpdateRequest request);

    void changeStatus(Order order, OrderStatus targetStatus, String note);

    void validateTransition(OrderStatus current, OrderStatus target);
}
