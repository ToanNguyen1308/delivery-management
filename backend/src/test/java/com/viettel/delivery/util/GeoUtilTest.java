package com.viettel.delivery.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilTest {

    @Test
    @DisplayName("Khoang cach giua hai diem trung nhau bang 0")
    void shouldReturnZeroForSamePoint() {
        double distance = GeoUtil.haversineKm(21.0285, 105.8542, 21.0285, 105.8542);

        assertThat(distance).isCloseTo(0d, within(0.0001));
    }

    @Test
    @DisplayName("Khoang cach Ha Noi - Ho Chi Minh khoang 1140 km theo duong chim bay")
    void shouldMatchKnownDistanceBetweenTwoCities() {
        double distance = GeoUtil.haversineKm(21.0285, 105.8542, 10.8231, 106.6297);

        assertThat(distance).isCloseTo(1140d, within(30d));
    }

    @Test
    @DisplayName("Khoang cach hai quan noi thanh Ha Noi nam trong khoang hop ly")
    void shouldComputeShortInnerCityDistance() {
        BigDecimal distance = GeoUtil.distanceKm(
                new BigDecimal("21.0313"), new BigDecimal("105.7967"),
                new BigDecimal("21.0285"), new BigDecimal("105.8542"));

        assertThat(distance.doubleValue()).isBetween(5d, 7d);
    }

    @Test
    @DisplayName("Thieu toa do thi tra ve 0 thay vi nem loi")
    void shouldReturnZeroWhenCoordinateMissing() {
        assertThat(GeoUtil.distanceKm(null, new BigDecimal("105.8"), new BigDecimal("21.0"), null))
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Quang duong duong bo lon hon duong chim bay 30%")
    void roadDistanceShouldBeLongerThanStraightLine() {
        BigDecimal straight = GeoUtil.distanceKm(
                new BigDecimal("21.0313"), new BigDecimal("105.7967"),
                new BigDecimal("21.0285"), new BigDecimal("105.8542"));
        BigDecimal road = GeoUtil.roadDistanceKm(
                new BigDecimal("21.0313"), new BigDecimal("105.7967"),
                new BigDecimal("21.0285"), new BigDecimal("105.8542"));

        assertThat(road.doubleValue())
                .isGreaterThan(straight.doubleValue())
                .isCloseTo(straight.doubleValue() * 1.3, within(0.05));
    }

    @Test
    @DisplayName("Ket qua duoc lam tron 2 chu so thap phan")
    void shouldRoundToTwoDecimals() {
        BigDecimal distance = GeoUtil.distanceKm(
                new BigDecimal("21.0313"), new BigDecimal("105.7967"),
                new BigDecimal("21.0285"), new BigDecimal("105.8542"));

        assertThat(distance.scale()).isEqualTo(2);
    }
}
