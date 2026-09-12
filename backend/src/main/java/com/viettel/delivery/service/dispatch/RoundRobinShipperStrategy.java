package com.viettel.delivery.service.dispatch;

import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Chia don luan phien theo thu tu de moi shipper deu co co hoi nhan don nhu nhau.
 */
@Component
public class RoundRobinShipperStrategy implements ShipperAssignmentStrategy {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public DispatchStrategyType getType() {
        return DispatchStrategyType.ROUND_ROBIN;
    }

    @Override
    public Optional<Shipper> selectShipper(Order order, List<Shipper> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        List<Shipper> sorted = candidates.stream()
                .sorted(Comparator.comparing(Shipper::getId))
                .toList();
        int index = (int) (counter.getAndIncrement() % sorted.size());
        return Optional.of(sorted.get(index));
    }
}
