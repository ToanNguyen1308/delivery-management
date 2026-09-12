package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.OrderCreateRequest;
import com.viettel.delivery.dto.request.OrderStatusUpdateRequest;
import com.viettel.delivery.dto.request.OrderUpdateRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.ExcelImportResponse;
import com.viettel.delivery.dto.response.OrderResponse;
import com.viettel.delivery.dto.response.OrderStatusHistoryResponse;
import com.viettel.delivery.dto.response.OrderSummaryResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import com.viettel.delivery.service.OrderExcelService;
import com.viettel.delivery.service.OrderService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "07. Quan ly don hang", description = "Tao, tra cuu, cap nhat trang thai va import/export don hang")
public class OrderController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final OrderService orderService;
    private final OrderExcelService orderExcelService;
    private final MessageUtil messageUtil;

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_ORDER_VIEW)
    @Operation(summary = "Tim kiem don hang",
            description = "Ket qua tu dong gioi han theo quyen: khach hang chi thay don cua minh, "
                    + "shipper chi thay don duoc phan cong")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> search(
            @Valid @RequestBody OrderSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ORDER_VIEW)
    @Operation(summary = "Chi tiet don hang")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    @GetMapping("/code/{orderCode}")
    @PreAuthorize(PermissionCode.HAS_ORDER_VIEW)
    @Operation(summary = "Chi tiet don hang theo ma van don")
    public ResponseEntity<ApiResponse<OrderResponse>> getByCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getByOrderCode(orderCode)));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize(PermissionCode.HAS_ORDER_VIEW)
    @Operation(summary = "Lich su chuyen trang thai cua don hang")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getStatusHistory(id)));
    }

    @PostMapping
    @PreAuthorize(PermissionCode.HAS_ORDER_CREATE)
    @Operation(summary = "Tao don hang", description = "He thong tu tinh cuoc va ap dung voucher neu hop le")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.create(request), messageUtil.get("success.common.created")));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ORDER_UPDATE)
    @Operation(summary = "Cap nhat don hang khi chua giao cho shipper")
    public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody OrderUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.update(id, request),
                messageUtil.get("success.common.updated")));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize(PermissionCode.HAS_ORDER_CONFIRM)
    @Operation(summary = "Xac nhan don hang", description = "Chuyen don tu CREATED sang CONFIRMED de dua vao dieu phoi")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.confirm(id),
                messageUtil.get("success.common.updated")));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize(PermissionCode.HAS_ORDER_VIEW)
    @Operation(summary = "Cap nhat trang thai don hang",
            description = "Chi chap nhan cac buoc chuyen hop le theo state machine cua don hang")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, request),
                messageUtil.get("success.common.updated")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ORDER_CANCEL)
    @Operation(summary = "Huy don hang")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id,
                                                    @RequestParam(required = false) String reason) {
        orderService.cancel(id, reason);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.updated")));
    }

    @PostMapping("/export")
    @PreAuthorize(PermissionCode.HAS_ORDER_EXPORT)
    @Operation(summary = "Xuat danh sach don hang ra Excel")
    public ResponseEntity<byte[]> export(@Valid @RequestBody OrderSearchRequest request) {
        byte[] content = orderExcelService.exportOrders(request);
        String fileName = "danh-sach-don-hang-%s.xlsx".formatted(LocalDate.now());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(fileName))
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(content);
    }

    @GetMapping("/import-template")
    @PreAuthorize(PermissionCode.HAS_ORDER_IMPORT)
    @Operation(summary = "Tai file Excel mau de import don hang")
    public ResponseEntity<byte[]> downloadTemplate() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mau-import-don-hang.xlsx\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(orderExcelService.buildImportTemplate());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(PermissionCode.HAS_ORDER_IMPORT)
    @Operation(summary = "Import don hang hang loat tu Excel",
            description = "Tra ve so dong thanh cong, so dong loi va chi tiet loi theo tung dong")
    public ResponseEntity<ApiResponse<ExcelImportResponse>> importOrders(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(orderExcelService.importOrders(file)));
    }
}
