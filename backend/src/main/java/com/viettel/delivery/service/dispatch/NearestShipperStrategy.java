package com.viettel.delivery.service.dispatch;

import com.viettel.delivery.config.properties.DispatchProperties;
import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chon shipper gan diem lay hang nhat trong ban kinh cho phep (cong thuc Haversine).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NearestShipperStrategy implements ShipperAssignmentStrategy {

    private final DispatchProperties dispatchProperties;

    @Override
    public DispatchStrategyType getType() {
        return DispatchStrategyType.NEAREST;
    }

    @Override
    public Optional<Shipper> selectShipper(Order order, List<Shipper> candidates) {
        if (order.getPickupLatitude() == null || order.getPickupLongitude() == null) {
            log.debug("Don {} khong co toa do lay hang, chuyen sang chon theo tai it nhat",
                    order.getOrderCode());
            return candidates.stream().min(Comparator.comparingInt(Shipper::getCurrentLoad));
        }

        return candidates.stream()
                .filter(Shipper::hasLocation)
                .filter(shipper -> distanceTo(order, shipper).doubleValue() <= dispatchProperties.maxRadiusKm())
                .min(Comparator.comparing(shipper -> distanceTo(order, shipper)));
    }

    public BigDecimal distanceTo(Order order, Shipper shipper) {
        return GeoUtil.distanceKm(shipper.getCurrentLatitude(), shipper.getCurrentLongitude(),
                order.getPickupLatitude(), order.getPickupLongitude());
    }
}
