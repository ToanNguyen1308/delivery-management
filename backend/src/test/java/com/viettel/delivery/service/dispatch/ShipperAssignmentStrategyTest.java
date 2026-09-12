package com.viettel.delivery.service.dispatch;

import com.viettel.delivery.config.properties.DispatchProperties;
import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.constant.enums.VehicleType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ShipperAssignmentStrategyTest {

    /** Diem lay hang tai Cau Giay, Ha Noi. */
    private static final BigDecimal PICKUP_LAT = new BigDecimal("21.0313");
    private static final BigDecimal PICKUP_LNG = new BigDecimal("105.7967");

    private Shipper shipper(long id, String code, String lat, String lng, int load, String rating) {
        return Shipper.builder()
                .id(id)
                .shipperCode(code)
                .user(User.builder().id(id).username(code).fullName("Shipper " + code).build())
                .vehicleType(VehicleType.MOTORBIKE)
                .status(ShipperStatus.ONLINE)
                .currentLatitude(lat == null ? null : new BigDecimal(lat))
                .currentLongitude(lng == null ? null : new BigDecimal(lng))
                .currentLoad(load)
                .maxConcurrentOrders(5)
                .rating(new BigDecimal(rating))
                .build();
    }

    private Order orderWithPickup(BigDecimal latitude, BigDecimal longitude) {
        return Order.builder()
                .orderCode("DH-TEST")
                .pickupLatitude(latitude)
                .pickupLongitude(longitude)
                .build();
    }

    @Test
    @DisplayName("Chien luoc NEAREST chon shipper gan diem lay hang nhat")
    void nearestStrategyShouldPickClosestShipper() {
        NearestShipperStrategy strategy = new NearestShipperStrategy(new DispatchProperties(
                DispatchStrategyType.NEAREST, 15));

        // SP02 o Cau Giay (rat gan), SP01 o Hoan Kiem (xa hon khoang 6km)
        List<Shipper> candidates = List.of(
                shipper(1L, "SP01", "21.0285", "105.8542", 0, "5.0"),
                shipper(2L, "SP02", "21.0320", "105.8000", 3, "4.0"));

        Optional<Shipper> selected = strategy.selectShipper(orderWithPickup(PICKUP_LAT, PICKUP_LNG), candidates);

        assertThat(selected).isPresent();
        assertThat(selected.get().getShipperCode()).isEqualTo("SP02");
    }

    @Test
    @DisplayName("Chien luoc NEAREST bo qua shipper ngoai ban kinh cho phep")
    void nearestStrategyShouldIgnoreShippersOutsideRadius() {
        NearestShipperStrategy strategy = new NearestShipperStrategy(new DispatchProperties(
                DispatchStrategyType.NEAREST, 2));

        List<Shipper> candidates = List.of(shipper(1L, "SP01", "21.0285", "105.8542", 0, "5.0"));

        Optional<Shipper> selected = strategy.selectShipper(orderWithPickup(PICKUP_LAT, PICKUP_LNG), candidates);

        assertThat(selected).isEmpty();
    }

    @Test
    @DisplayName("Don khong co toa do thi NEAREST chuyen sang chon theo tai it nhat")
    void nearestStrategyShouldFallbackWhenOrderHasNoCoordinates() {
        NearestShipperStrategy strategy = new NearestShipperStrategy(new DispatchProperties(
                DispatchStrategyType.NEAREST, 15));

        List<Shipper> candidates = List.of(
                shipper(1L, "SP01", "21.0285", "105.8542", 4, "5.0"),
                shipper(2L, "SP02", "21.0320", "105.8000", 1, "4.0"));

        Optional<Shipper> selected = strategy.selectShipper(orderWithPickup(null, null), candidates);

        assertThat(selected).isPresent();
        assertThat(selected.get().getShipperCode()).isEqualTo("SP02");
    }

    @Test
    @DisplayName("Chien luoc LEAST_LOAD chon shipper dang it don nhat")
    void leastLoadStrategyShouldPickLowestLoad() {
        LeastLoadShipperStrategy strategy = new LeastLoadShipperStrategy();

        List<Shipper> candidates = List.of(
                shipper(1L, "SP01", "21.0285", "105.8542", 4, "5.0"),
                shipper(2L, "SP02", "21.0320", "105.8000", 1, "4.0"),
                shipper(3L, "SP03", "21.0450", "105.8900", 2, "4.9"));

        Optional<Shipper> selected = strategy.selectShipper(orderWithPickup(PICKUP_LAT, PICKUP_LNG), candidates);

        assertThat(selected).isPresent();
        assertThat(selected.get().getShipperCode()).isEqualTo("SP02");
    }

    @Test
    @DisplayName("LEAST_LOAD uu tien danh gia cao hon khi tai bang nhau")
    void leastLoadStrategyShouldPreferHigherRatingOnTie() {
        LeastLoadShipperStrategy strategy = new LeastLoadShipperStrategy();

        List<Shipper> candidates = List.of(
                shipper(1L, "SP01", "21.0285", "105.8542", 2, "4.2"),
                shipper(2L, "SP02", "21.0320", "105.8000", 2, "4.8"));

        Optional<Shipper> selected = strategy.selectShipper(orderWithPickup(PICKUP_LAT, PICKUP_LNG), candidates);

        assertThat(selected.orElseThrow().getShipperCode()).isEqualTo("SP02");
    }

    @Test
    @DisplayName("Chien luoc ROUND_ROBIN chia don luan phien theo thu tu")
    void roundRobinStrategyShouldRotateShippers() {
        RoundRobinShipperStrategy strategy = new RoundRobinShipperStrategy();
        Order order = orderWithPickup(PICKUP_LAT, PICKUP_LNG);

        List<Shipper> candidates = List.of(
                shipper(1L, "SP01", "21.0285", "105.8542", 0, "5.0"),
                shipper(2L, "SP02", "21.0320", "105.8000", 0, "5.0"));

        assertThat(strategy.selectShipper(order, candidates).orElseThrow().getShipperCode()).isEqualTo("SP01");
        assertThat(strategy.selectShipper(order, candidates).orElseThrow().getShipperCode()).isEqualTo("SP02");
        assertThat(strategy.selectShipper(order, candidates).orElseThrow().getShipperCode()).isEqualTo("SP01");
    }

    @Test
    @DisplayName("Khong co shipper nao thi khong chon duoc ai")
    void shouldReturnEmptyWhenNoCandidate() {
        Order order = orderWithPickup(PICKUP_LAT, PICKUP_LNG);

        assertThat(new RoundRobinShipperStrategy().selectShipper(order, List.of())).isEmpty();
        assertThat(new LeastLoadShipperStrategy().selectShipper(order, List.of())).isEmpty();
    }

    @Test
    @DisplayName("Moi chien luoc khai bao dung loai cua minh")
    void shouldExposeCorrectStrategyType() {
        assertThat(new NearestShipperStrategy(new DispatchProperties(DispatchStrategyType.NEAREST, 15)).getType())
                .isEqualTo(DispatchStrategyType.NEAREST);
        assertThat(new LeastLoadShipperStrategy().getType()).isEqualTo(DispatchStrategyType.LEAST_LOAD);
        assertThat(new RoundRobinShipperStrategy().getType()).isEqualTo(DispatchStrategyType.ROUND_ROBIN);
    }
}
