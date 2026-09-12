package com.viettel.delivery.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Schema(name = "StatisticRangeRequest", description = "Khoang thoi gian thong ke")
public class StatisticRangeRequest implements Serializable {

    private static final int DEFAULT_RANGE_DAYS = 30;

    @Schema(description = "Bo trong se lay 30 ngay gan nhat")
    private LocalDate fromDate;

    private LocalDate toDate;

    @Schema(description = "So ban ghi cho bang xep hang top shipper", example = "5")
    private Integer topSize;

    public LocalDateTime from() {
        LocalDate date = fromDate != null ? fromDate : LocalDate.now().minusDays(DEFAULT_RANGE_DAYS);
        return date.atStartOfDay();
    }

    public LocalDateTime to() {
        LocalDate date = toDate != null ? toDate : LocalDate.now();
        return date.atTime(LocalTime.MAX);
    }

    public int topSizeOrDefault() {
        return topSize == null || topSize < 1 ? 5 : Math.min(topSize, 20);
    }

    /**
     * Khoa cache Redis phai bao gom khoang ngay de tranh tra ve so lieu cu.
     */
    public String cacheKey() {
        return "%s_%s_%s".formatted(from().toLocalDate(), to().toLocalDate(), topSizeOrDefault());
    }
}
