package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.TrackingEventType;
import com.viettel.delivery.dto.request.LocationUpdateRequest;
import com.viettel.delivery.dto.response.OrderTrackingResponse;
import com.viettel.delivery.dto.response.ShipperLocationResponse;
import com.viettel.delivery.entity.Order;

import java.math.BigDecimal;

public interface TrackingService {

    ShipperLocationResponse pushLocation(LocationUpdateRequest request);

    /**
     * Tra cuu hanh trinh cho nguoi dung da dang nhap (kiem tra quyen truy cap du lieu).
     */
    OrderTrackingResponse getTrackingById(Long orderId);

    /**
     * Tra cuu cong khai theo ma van don, du lieu nhay cam duoc che bot.
     */
    OrderTrackingResponse getPublicTracking(String orderCode);

    void recordEvent(Order order, TrackingEventType eventType, String description,
                     BigDecimal latitude, BigDecimal longitude);
}
