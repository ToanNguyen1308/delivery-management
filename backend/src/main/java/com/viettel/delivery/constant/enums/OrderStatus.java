package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Trang thai don hang kem bang chuyen tiep hop le (state machine).
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements BaseEnum {

    CREATED("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    ASSIGNED("Đã phân công shipper"),
    PICKED_UP("Đã lấy hàng"),
    IN_TRANSIT("Đang vận chuyển"),
    DELIVERED("Giao thành công"),
    FAILED("Giao thất bại"),
    RETURNED("Đã hoàn trả"),
    CANCELLED("Đã hủy");

    private final String description;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(CREATED, EnumSet.of(CONFIRMED, CANCELLED));
        TRANSITIONS.put(CONFIRMED, EnumSet.of(ASSIGNED, CANCELLED));
        TRANSITIONS.put(ASSIGNED, EnumSet.of(PICKED_UP, CONFIRMED, CANCELLED));
        TRANSITIONS.put(PICKED_UP, EnumSet.of(IN_TRANSIT, FAILED));
        TRANSITIONS.put(IN_TRANSIT, EnumSet.of(DELIVERED, FAILED));
        TRANSITIONS.put(FAILED, EnumSet.of(IN_TRANSIT, RETURNED));
        TRANSITIONS.put(DELIVERED, Collections.emptySet());
        TRANSITIONS.put(RETURNED, Collections.emptySet());
        TRANSITIONS.put(CANCELLED, Collections.emptySet());
    }

    public boolean canTransitionTo(OrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    public Set<OrderStatus> nextStatuses() {
        return Collections.unmodifiableSet(TRANSITIONS.getOrDefault(this, Collections.emptySet()));
    }

    public boolean isFinal() {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).isEmpty();
    }

    public boolean isEditable() {
        return this == CREATED || this == CONFIRMED;
    }

    public static Set<OrderStatus> activeStatuses() {
        return EnumSet.of(CREATED, CONFIRMED, ASSIGNED, PICKED_UP, IN_TRANSIT);
    }
}
