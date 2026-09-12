package com.viettel.delivery.service;

import com.viettel.delivery.dto.response.DashboardOverviewResponse;
import com.viettel.delivery.dto.response.statistic.OrderCountByDate;
import com.viettel.delivery.dto.response.statistic.OrderCountByStatus;
import com.viettel.delivery.dto.response.statistic.RevenueByProvince;
import com.viettel.delivery.dto.response.statistic.TopShipperStatistic;
import com.viettel.delivery.dto.search.StatisticRangeRequest;

import java.util.List;

public interface DashboardService {

    /**
     * Chi so tong quan. Ket qua tu dong gioi han theo vai tro cua nguoi dang dang nhap.
     */
    DashboardOverviewResponse getOverview(StatisticRangeRequest request);

    List<OrderCountByStatus> getOrdersByStatus(StatisticRangeRequest request);

    List<OrderCountByDate> getOrdersByDate(StatisticRangeRequest request);

    List<RevenueByProvince> getRevenueByProvince(StatisticRangeRequest request);

    List<TopShipperStatistic> getTopShippers(StatisticRangeRequest request);
}
