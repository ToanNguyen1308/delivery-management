package com.viettel.delivery.dto.response.statistic;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cap moc thoi gian dung de tinh thoi gian giao hang trung binh,
 * tach rieng de truy van chi select hai cot can thiet.
 */
public record DeliveryDuration(LocalDateTime createdDate, LocalDateTime deliveredAt) {

    public long minutes() {
        if (createdDate == null || deliveredAt == null) {
            return 0;
        }
        return Duration.between(createdDate, deliveredAt).toMinutes();
    }
}
