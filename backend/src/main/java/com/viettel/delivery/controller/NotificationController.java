package com.viettel.delivery.controller;

import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.NotificationResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.service.NotificationService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "13. Thong bao", description = "Thong bao trong ung dung, day realtime qua WebSocket")
public class NotificationController {

    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    @PostMapping("/search")
    @Operation(summary = "Danh sach thong bao cua toi")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> search(
            @Valid @RequestBody NotificationSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(request)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "So thong bao chua doc")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.countMyUnread()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Danh dau mot thong bao da doc")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.updated")));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Danh dau tat ca thong bao da doc")
    public ResponseEntity<ApiResponse<Integer>> markAllRead() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAllRead(),
                messageUtil.get("success.common.updated")));
    }

    @Getter
    @Setter
    public static class NotificationSearchRequest extends BaseSearchRequest {
    }
}
