package com.viettel.delivery.constant.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("Luong chuyen trang thai chuan duoc cho phep")
    void shouldAllowHappyPathTransitions() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.ASSIGNED)).isTrue();
        assertThat(OrderStatus.ASSIGNED.canTransitionTo(OrderStatus.PICKED_UP)).isTrue();
        assertThat(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.IN_TRANSIT)).isTrue();
        assertThat(OrderStatus.IN_TRANSIT.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("Khong duoc nhay bo qua cac buoc trung gian")
    void shouldRejectSkippingSteps() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.ASSIGNED)).isFalse();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PICKED_UP)).isFalse();
        assertThat(OrderStatus.ASSIGNED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    @DisplayName("Don da giao, da hoan tra hoac da huy la trang thai ket thuc")
    void shouldTreatTerminalStatusesAsFinal() {
        assertThat(OrderStatus.DELIVERED.isFinal()).isTrue();
        assertThat(OrderStatus.RETURNED.isFinal()).isTrue();
        assertThat(OrderStatus.CANCELLED.isFinal()).isTrue();
        assertThat(OrderStatus.DELIVERED.nextStatuses()).isEmpty();
    }

    @Test
    @DisplayName("Chi huy duoc don khi chua lay hang")
    void shouldAllowCancelOnlyBeforePickup() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.ASSIGNED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.IN_TRANSIT.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    @DisplayName("Giao that bai co the giao lai hoac hoan tra")
    void shouldAllowRetryOrReturnAfterFailure() {
        assertThat(OrderStatus.FAILED.canTransitionTo(OrderStatus.IN_TRANSIT)).isTrue();
        assertThat(OrderStatus.FAILED.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        assertThat(OrderStatus.FAILED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    @DisplayName("Shipper tu choi thi don quay lai trang thai da xac nhan")
    void shouldAllowReturningToConfirmedWhenShipperRejects() {
        assertThat(OrderStatus.ASSIGNED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
    }

    @Test
    @DisplayName("Chi sua duoc don khi chua phan cong shipper")
    void shouldBeEditableOnlyBeforeAssignment() {
        assertThat(OrderStatus.CREATED.isEditable()).isTrue();
        assertThat(OrderStatus.CONFIRMED.isEditable()).isTrue();
        assertThat(OrderStatus.ASSIGNED.isEditable()).isFalse();
        assertThat(OrderStatus.DELIVERED.isEditable()).isFalse();
    }

    @Test
    @DisplayName("Lay hang, giao hang va hoan tra la thao tac cua shipper")
    void deliveryStatusesShouldBeShipperOperations() {
        assertThat(OrderStatus.PICKED_UP.isShipperOperation()).isTrue();
        assertThat(OrderStatus.IN_TRANSIT.isShipperOperation()).isTrue();
        assertThat(OrderStatus.DELIVERED.isShipperOperation()).isTrue();
        assertThat(OrderStatus.FAILED.isShipperOperation()).isTrue();
        assertThat(OrderStatus.RETURNED.isShipperOperation()).isTrue();
        assertThat(OrderStatus.CREATED.isShipperOperation()).isFalse();
        assertThat(OrderStatus.CONFIRMED.isShipperOperation()).isFalse();
        assertThat(OrderStatus.ASSIGNED.isShipperOperation()).isFalse();
        assertThat(OrderStatus.CANCELLED.isShipperOperation()).isFalse();
    }

    @Test
    @DisplayName("Tap trang thai dang xu ly khong chua trang thai ket thuc")
    void activeStatusesShouldExcludeFinalOnes() {
        assertThat(OrderStatus.activeStatuses())
                .containsExactlyInAnyOrder(OrderStatus.CREATED, OrderStatus.CONFIRMED,
                        OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT)
                .doesNotContain(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Moi trang thai deu co mo ta tieng Viet de hien thi")
    void everyStatusShouldHaveDescription() {
        for (OrderStatus status : OrderStatus.values()) {
            assertThat(status.getDescription()).isNotBlank();
        }
    }
}
