package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.NotificationType;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.NotificationResponse;
import com.viettel.delivery.dto.response.PageResponse;

public interface NotificationService {

    /**
     * Tao thong bao trong database va day realtime den nguoi nhan qua WebSocket.
     */
    void notifyUser(Long userId, NotificationType type, String title, String content,
                    String referenceType, Long referenceId, String referenceCode);

    PageResponse<NotificationResponse> getMyNotifications(BaseSearchRequest request);

    long countMyUnread();

    void markRead(Long notificationId);

    int markAllRead();
}
