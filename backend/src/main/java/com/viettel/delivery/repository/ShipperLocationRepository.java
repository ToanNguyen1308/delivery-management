package com.viettel.delivery.repository;

import com.viettel.delivery.entity.ShipperLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperLocationRepository extends JpaRepository<ShipperLocation, Long> {

    @Query("""
            SELECT sl FROM ShipperLocation sl
            WHERE sl.orderId = :orderId AND sl.isDeleted = false
            ORDER BY sl.recordedAt ASC
            """)
    List<ShipperLocation> findRouteByOrderId(@Param("orderId") Long orderId);

    @Query("""
            SELECT sl FROM ShipperLocation sl
            WHERE sl.shipper.id = :shipperId AND sl.isDeleted = false
            ORDER BY sl.recordedAt DESC
            """)
    List<ShipperLocation> findLatestByShipperId(@Param("shipperId") Long shipperId, Pageable pageable);
}
