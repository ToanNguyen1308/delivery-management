package com.viettel.delivery.repository;

import com.viettel.delivery.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query("""
            SELECT h FROM OrderStatusHistory h
            WHERE h.order.id = :orderId AND h.isDeleted = false
            ORDER BY h.changedAt ASC
            """)
    List<OrderStatusHistory> findByOrderId(@Param("orderId") Long orderId);
}
