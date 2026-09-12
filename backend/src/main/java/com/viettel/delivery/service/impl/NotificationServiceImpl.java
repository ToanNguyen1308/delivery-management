package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.NotificationType;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.NotificationResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.entity.Notification;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.TrackingMapper;
import com.viettel.delivery.repository.NotificationRepository;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TrackingMapper trackingMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void notifyUser(Long userId, NotificationType type, String title, String content,
                           String referenceType, Long referenceId, String referenceCode) {
        if (userId == null) {
            return;
        }
        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElse(null);
        if (user == null) {
            log.warn("Bo qua thong bao vi khong tim thay nguoi dung {}", userId);
            return;
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .referenceCode(referenceCode)
                .isRead(Boolean.FALSE)
                .build());

        pushRealtime(user.getUsername(), notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(BaseSearchRequest request) {
        Page<Notification> page = notificationRepository
                .findByUserId(SecurityUtil.getCurrentUserId(), request.toUnsortedPageable());
        return PageResponse.of(page, trackingMapper::toNotificationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread() {
        return notificationRepository.countUnread(SecurityUtil.getCurrentUserId());
    }

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
        notification.markRead();
    }

    @Override
    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(SecurityUtil.getCurrentUserId(), LocalDateTime.now());
    }

    /**
     * Loi WebSocket khong duoc lam hong nghiep vu, thong bao van da luu trong database.
     */
    private void pushRealtime(String username, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(username, AppConstants.WS_QUEUE_NOTIFICATION,
                    trackingMapper.toNotificationResponse(notification));
        } catch (Exception ex) {
            log.warn("Khong day duoc thong bao realtime cho {}: {}", username, ex.getMessage());
        }
    }
}
