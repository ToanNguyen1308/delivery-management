package com.viettel.delivery.event;

import com.viettel.delivery.constant.enums.OrderStatus;

import java.math.BigDecimal;

/**
 * Phat moi khi don hang doi trang thai. Cac listener se ghi tracking, gui thong bao
 * va day du lieu realtime qua WebSocket.
 */
public record OrderStatusChangedEvent(Long orderId,
                                      String orderCode,
                                      Long customerId,
                                      Long shipperUserId,
                                      OrderStatus fromStatus,
                                      OrderStatus toStatus,
                                      String note,
                                      BigDecimal latitude,
                                      BigDecimal longitude) {
}
