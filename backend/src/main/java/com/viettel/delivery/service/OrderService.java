package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.OrderCreateRequest;
import com.viettel.delivery.dto.request.OrderStatusUpdateRequest;
import com.viettel.delivery.dto.request.OrderUpdateRequest;
import com.viettel.delivery.dto.response.OrderResponse;
import com.viettel.delivery.dto.response.OrderStatusHistoryResponse;
import com.viettel.delivery.dto.response.OrderSummaryResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import com.viettel.delivery.entity.Order;

import java.util.List;

public interface OrderService {

    PageResponse<OrderSummaryResponse> search(OrderSearchRequest request);

    OrderResponse getById(Long id);

    OrderResponse getByOrderCode(String orderCode);

    OrderResponse create(OrderCreateRequest request);

    /**
     * Tao don thay cho mot khach hang khac, dung khi dieu phoi vien import don tu Excel.
     */
    OrderResponse createForCustomer(OrderCreateRequest request, Long customerId);

    OrderResponse update(Long id, OrderUpdateRequest request);

    OrderResponse confirm(Long id);

    OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);

    void cancel(Long id, String reason);

    List<OrderStatusHistoryResponse> getStatusHistory(Long id);

    /**
     * Dung cho cac service khac trong he thong, da kiem tra quyen truy cap du lieu.
     */
    Order getAccessibleOrder(Long id);
}
