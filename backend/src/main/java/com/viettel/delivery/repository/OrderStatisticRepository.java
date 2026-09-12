package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.dto.response.statistic.DeliveryDuration;
import com.viettel.delivery.dto.response.statistic.OrderCountByDate;
import com.viettel.delivery.dto.response.statistic.OrderCountByStatus;
import com.viettel.delivery.dto.response.statistic.RevenueByProvince;
import com.viettel.delivery.dto.response.statistic.TopShipperStatistic;
import com.viettel.delivery.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Cac truy van thong ke, viet bang JPQL voi DTO Projection de chi lay dung cot can dung.
 */
public interface OrderStatisticRepository extends Repository<Order, Long> {

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.isDeleted = false
              AND o.createdDate BETWEEN :from AND :to
            """)
    long countOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.isDeleted = false
              AND o.status IN :statuses
              AND o.createdDate BETWEEN :from AND :to
            """)
    long countOrdersByStatuses(@Param("statuses") Collection<OrderStatus> statuses,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.isDeleted = false
              AND o.status = :status
              AND o.createdDate BETWEEN :from AND :to
            """)
    BigDecimal sumRevenue(@Param("status") OrderStatus status,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.codAmount), 0) FROM Order o
            WHERE o.isDeleted = false
              AND o.status = :status
              AND o.createdDate BETWEEN :from AND :to
            """)
    BigDecimal sumCodAmount(@Param("status") OrderStatus status,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("""
            SELECT new com.viettel.delivery.dto.response.statistic.OrderCountByStatus(o.status, COUNT(o))
            FROM Order o
            WHERE o.isDeleted = false
              AND o.createdDate BETWEEN :from AND :to
            GROUP BY o.status
            ORDER BY COUNT(o) DESC
            """)
    List<OrderCountByStatus> countGroupByStatus(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    @Query("""
            SELECT new com.viettel.delivery.dto.response.statistic.OrderCountByDate(
                YEAR(o.createdDate), MONTH(o.createdDate), DAY(o.createdDate),
                COUNT(o), COALESCE(SUM(o.totalAmount), 0))
            FROM Order o
            WHERE o.isDeleted = false
              AND o.createdDate BETWEEN :from AND :to
            GROUP BY YEAR(o.createdDate), MONTH(o.createdDate), DAY(o.createdDate)
            ORDER BY YEAR(o.createdDate), MONTH(o.createdDate), DAY(o.createdDate)
            """)
    List<OrderCountByDate> countGroupByDate(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("""
            SELECT new com.viettel.delivery.dto.response.statistic.RevenueByProvince(
                o.deliveryProvince, COUNT(o), COALESCE(SUM(o.totalAmount), 0))
            FROM Order o
            WHERE o.isDeleted = false
              AND o.createdDate BETWEEN :from AND :to
            GROUP BY o.deliveryProvince
            ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC
            """)
    List<RevenueByProvince> revenueGroupByProvince(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    @Query("""
            SELECT new com.viettel.delivery.dto.response.statistic.TopShipperStatistic(
                s.id, s.shipperCode, u.fullName, COUNT(o), s.rating)
            FROM Order o
            JOIN o.currentShipper s
            JOIN s.user u
            WHERE o.isDeleted = false
              AND o.status = :status
              AND o.createdDate BETWEEN :from AND :to
            GROUP BY s.id, s.shipperCode, u.fullName, s.rating
            ORDER BY COUNT(o) DESC
            """)
    List<TopShipperStatistic> findTopShippers(@Param("status") OrderStatus status,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              Pageable pageable);

    @Query("""
            SELECT new com.viettel.delivery.dto.response.statistic.DeliveryDuration(o.createdDate, o.deliveredAt)
            FROM Order o
            WHERE o.isDeleted = false
              AND o.status = :status
              AND o.deliveredAt IS NOT NULL
              AND o.createdDate BETWEEN :from AND :to
            """)
    List<DeliveryDuration> findDeliveryDurations(@Param("status") OrderStatus status,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    /* ---------- Thong ke rieng cho tung khach hang ---------- */

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.isDeleted = false AND o.customer.id = :customerId
              AND o.createdDate BETWEEN :from AND :to
            """)
    long countOrdersByCustomer(@Param("customerId") Long customerId,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.isDeleted = false AND o.customer.id = :customerId
              AND o.status IN :statuses
              AND o.createdDate BETWEEN :from AND :to
            """)
    long countOrdersByCustomerAndStatuses(@Param("customerId") Long customerId,
                                          @Param("statuses") Collection<OrderStatus> statuses,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.isDeleted = false AND o.customer.id = :customerId
              AND o.createdDate BETWEEN :from AND :to
            """)
    BigDecimal sumSpendingByCustomer(@Param("customerId") Long customerId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /* ---------- Thong ke rieng cho tung shipper ---------- */

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.isDeleted = false AND o.currentShipper.id = :shipperId
              AND o.status IN :statuses
              AND o.createdDate BETWEEN :from AND :to
            """)
    long countOrdersByShipperAndStatuses(@Param("shipperId") Long shipperId,
                                         @Param("statuses") Collection<OrderStatus> statuses,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
