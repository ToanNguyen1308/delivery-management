package com.viettel.delivery.repository;

import com.viettel.delivery.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    @Query("""
            SELECT t FROM TrackingEvent t
            WHERE t.order.id = :orderId AND t.isDeleted = false
            ORDER BY t.occurredAt ASC
            """)
    List<TrackingEvent> findByOrderId(@Param("orderId") Long orderId);
}
