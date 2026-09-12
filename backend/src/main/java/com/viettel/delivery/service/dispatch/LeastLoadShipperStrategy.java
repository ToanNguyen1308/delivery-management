package com.viettel.delivery.service.dispatch;

import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chon shipper dang giu it don nhat, neu bang nhau thi uu tien nguoi co danh gia cao hon.
 */
@Component
public class LeastLoadShipperStrategy implements ShipperAssignmentStrategy {

    @Override
    public DispatchStrategyType getType() {
        return DispatchStrategyType.LEAST_LOAD;
    }

    @Override
    public Optional<Shipper> selectShipper(Order order, List<Shipper> candidates) {
        return candidates.stream()
                .min(Comparator.comparingInt(Shipper::getCurrentLoad)
                        .thenComparing(Shipper::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Shipper::getId));
    }
}
