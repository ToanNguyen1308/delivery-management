package com.viettel.delivery.service.dispatch;

import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;

import java.util.List;
import java.util.Optional;

/**
 * Chien luoc chon shipper khi gan don tu dong (Strategy pattern).
 */
public interface ShipperAssignmentStrategy {

    DispatchStrategyType getType();

    Optional<Shipper> selectShipper(Order order, List<Shipper> candidates);
}
