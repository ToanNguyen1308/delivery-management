package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.DashboardOverviewResponse;
import com.viettel.delivery.dto.response.statistic.OrderCountByDate;
import com.viettel.delivery.dto.response.statistic.OrderCountByStatus;
import com.viettel.delivery.dto.response.statistic.RevenueByProvince;
import com.viettel.delivery.dto.response.statistic.TopShipperStatistic;
import com.viettel.delivery.dto.search.StatisticRangeRequest;
import com.viettel.delivery.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "14. Dashboard", description = "Chi so va bieu do thong ke")
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping("/overview")
    @PreAuthorize(PermissionCode.HAS_DASHBOARD_VIEW)
    @Operation(summary = "Chi so tong quan",
            description = "Admin va dieu phoi vien thay so lieu toan he thong, "
                    + "khach hang va shipper chi thay so lieu cua minh")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> overview(
            @Valid @RequestBody StatisticRangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOverview(request)));
    }

    @PostMapping("/orders-by-status")
    @PreAuthorize(PermissionCode.HAS_DASHBOARD_ADMIN)
    @Operation(summary = "Ti le don hang theo trang thai")
    public ResponseEntity<ApiResponse<List<OrderCountByStatus>>> ordersByStatus(
            @Valid @RequestBody StatisticRangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOrdersByStatus(request)));
    }

    @PostMapping("/orders-by-date")
    @PreAuthorize(PermissionCode.HAS_DASHBOARD_ADMIN)
    @Operation(summary = "So don va doanh thu theo ngay")
    public ResponseEntity<ApiResponse<List<OrderCountByDate>>> ordersByDate(
            @Valid @RequestBody StatisticRangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOrdersByDate(request)));
    }

    @PostMapping("/revenue-by-province")
    @PreAuthorize(PermissionCode.HAS_DASHBOARD_ADMIN)
    @Operation(summary = "Doanh thu theo tinh thanh")
    public ResponseEntity<ApiResponse<List<RevenueByProvince>>> revenueByProvince(
            @Valid @RequestBody StatisticRangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRevenueByProvince(request)));
    }

    @PostMapping("/top-shippers")
    @PreAuthorize(PermissionCode.HAS_DASHBOARD_ADMIN)
    @Operation(summary = "Bang xep hang shipper giao nhieu don nhat")
    public ResponseEntity<ApiResponse<List<TopShipperStatistic>>> topShippers(
            @Valid @RequestBody StatisticRangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTopShippers(request)));
    }
}
