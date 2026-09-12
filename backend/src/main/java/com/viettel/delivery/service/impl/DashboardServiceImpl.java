package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.response.DashboardOverviewResponse;
import com.viettel.delivery.dto.response.statistic.DeliveryDuration;
import com.viettel.delivery.dto.response.statistic.OrderCountByDate;
import com.viettel.delivery.dto.response.statistic.OrderCountByStatus;
import com.viettel.delivery.dto.response.statistic.RevenueByProvince;
import com.viettel.delivery.dto.response.statistic.TopShipperStatistic;
import com.viettel.delivery.dto.search.StatisticRangeRequest;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.repository.OrderStatisticRepository;
import com.viettel.delivery.repository.ShipperRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);
    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final Set<OrderStatus> FINISHED_STATUSES =
            EnumSet.of(OrderStatus.DELIVERED, OrderStatus.FAILED, OrderStatus.RETURNED);

    private final OrderStatisticRepository statisticRepository;
    private final ShipperRepository shipperRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(StatisticRangeRequest request) {
        return SecurityUtil.canViewAllData()
                ? buildSystemOverview(request)
                : buildPersonalOverview(request);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = AppConstants.CACHE_DASHBOARD, key = "'status_' + #request.cacheKey()")
    public List<OrderCountByStatus> getOrdersByStatus(StatisticRangeRequest request) {
        return statisticRepository.countGroupByStatus(request.from(), request.to());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = AppConstants.CACHE_DASHBOARD, key = "'date_' + #request.cacheKey()")
    public List<OrderCountByDate> getOrdersByDate(StatisticRangeRequest request) {
        return statisticRepository.countGroupByDate(request.from(), request.to());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = AppConstants.CACHE_DASHBOARD, key = "'province_' + #request.cacheKey()")
    public List<RevenueByProvince> getRevenueByProvince(StatisticRangeRequest request) {
        return statisticRepository.revenueGroupByProvince(request.from(), request.to());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = AppConstants.CACHE_DASHBOARD, key = "'topShipper_' + #request.cacheKey()")
    public List<TopShipperStatistic> getTopShippers(StatisticRangeRequest request) {
        return statisticRepository.findTopShippers(OrderStatus.DELIVERED, request.from(), request.to(),
                PageRequest.of(0, request.topSizeOrDefault()));
    }

    private DashboardOverviewResponse buildSystemOverview(StatisticRangeRequest request) {
        LocalDateTime from = request.from();
        LocalDateTime to = request.to();

        long totalOrders = statisticRepository.countOrders(from, to);
        long delivered = statisticRepository.countOrdersByStatuses(EnumSet.of(OrderStatus.DELIVERED), from, to);
        long cancelled = statisticRepository.countOrdersByStatuses(EnumSet.of(OrderStatus.CANCELLED), from, to);
        long inProgress = statisticRepository.countOrdersByStatuses(OrderStatus.activeStatuses(), from, to);
        long finished = statisticRepository.countOrdersByStatuses(FINISHED_STATUSES, from, to);

        return DashboardOverviewResponse.builder()
                .totalOrders(totalOrders)
                .ordersToday(statisticRepository.countOrders(startOfToday(), endOfToday()))
                .deliveredOrders(delivered)
                .inProgressOrders(inProgress)
                .cancelledOrders(cancelled)
                .successRate(percentage(delivered, finished))
                .totalRevenue(statisticRepository.sumRevenue(OrderStatus.DELIVERED, from, to))
                .revenueToday(statisticRepository.sumRevenue(OrderStatus.DELIVERED, startOfToday(), endOfToday()))
                .totalCodCollected(statisticRepository.sumCodAmount(OrderStatus.DELIVERED, from, to))
                .totalShippers(shipperRepository.count())
                .onlineShippers(shipperRepository.countByStatus(ShipperStatus.ONLINE)
                        + shipperRepository.countByStatus(ShipperStatus.BUSY))
                .averageDeliveryHours(averageDeliveryHours(from, to))
                .build();
    }

    /**
     * Khach hang thay so lieu don cua minh, shipper thay so lieu don duoc giao cho minh.
     */
    private DashboardOverviewResponse buildPersonalOverview(StatisticRangeRequest request) {
        LocalDateTime from = request.from();
        LocalDateTime to = request.to();

        if (SecurityUtil.hasAuthority(PermissionCode.SHIPPER_SELF)) {
            Shipper shipper = shipperRepository.findByUserIdWithUser(SecurityUtil.getCurrentUserId()).orElse(null);
            if (shipper == null) {
                return emptyOverview();
            }
            long delivered = statisticRepository.countOrdersByShipperAndStatuses(shipper.getId(),
                    EnumSet.of(OrderStatus.DELIVERED), from, to);
            long failed = statisticRepository.countOrdersByShipperAndStatuses(shipper.getId(),
                    EnumSet.of(OrderStatus.FAILED, OrderStatus.RETURNED), from, to);
            long inProgress = statisticRepository.countOrdersByShipperAndStatuses(shipper.getId(),
                    OrderStatus.activeStatuses(), from, to);

            return DashboardOverviewResponse.builder()
                    .totalOrders(delivered + failed + inProgress)
                    .deliveredOrders(delivered)
                    .inProgressOrders(inProgress)
                    .cancelledOrders(failed)
                    .successRate(percentage(delivered, delivered + failed))
                    .totalRevenue(BigDecimal.ZERO)
                    .revenueToday(BigDecimal.ZERO)
                    .totalCodCollected(BigDecimal.ZERO)
                    .averageDeliveryHours(BigDecimal.ZERO)
                    .build();
        }

        Long customerId = SecurityUtil.getCurrentUserId();
        long totalOrders = statisticRepository.countOrdersByCustomer(customerId, from, to);
        long delivered = statisticRepository.countOrdersByCustomerAndStatuses(customerId,
                EnumSet.of(OrderStatus.DELIVERED), from, to);
        long cancelled = statisticRepository.countOrdersByCustomerAndStatuses(customerId,
                EnumSet.of(OrderStatus.CANCELLED), from, to);
        long inProgress = statisticRepository.countOrdersByCustomerAndStatuses(customerId,
                OrderStatus.activeStatuses(), from, to);

        return DashboardOverviewResponse.builder()
                .totalOrders(totalOrders)
                .ordersToday(statisticRepository.countOrdersByCustomer(customerId, startOfToday(), endOfToday()))
                .deliveredOrders(delivered)
                .inProgressOrders(inProgress)
                .cancelledOrders(cancelled)
                .successRate(percentage(delivered, delivered + cancelled))
                .totalRevenue(statisticRepository.sumSpendingByCustomer(customerId, from, to))
                .revenueToday(statisticRepository.sumSpendingByCustomer(customerId, startOfToday(), endOfToday()))
                .totalCodCollected(BigDecimal.ZERO)
                .averageDeliveryHours(BigDecimal.ZERO)
                .build();
    }

    private BigDecimal averageDeliveryHours(LocalDateTime from, LocalDateTime to) {
        List<DeliveryDuration> durations =
                statisticRepository.findDeliveryDurations(OrderStatus.DELIVERED, from, to);
        if (durations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long totalMinutes = durations.stream().mapToLong(DeliveryDuration::minutes).sum();
        return BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(durations.size()), 2, RoundingMode.HALF_UP)
                .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(PERCENT)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private DashboardOverviewResponse emptyOverview() {
        return DashboardOverviewResponse.builder()
                .successRate(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.ZERO)
                .revenueToday(BigDecimal.ZERO)
                .totalCodCollected(BigDecimal.ZERO)
                .averageDeliveryHours(BigDecimal.ZERO)
                .build();
    }

    private LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime endOfToday() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }
}
