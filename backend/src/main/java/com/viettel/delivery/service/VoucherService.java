package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.VoucherRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.VoucherResponse;
import com.viettel.delivery.dto.search.VoucherSearchRequest;
import com.viettel.delivery.model.VoucherDiscountResult;

import java.math.BigDecimal;

public interface VoucherService {

    PageResponse<VoucherResponse> search(VoucherSearchRequest request);

    VoucherResponse getById(Long id);

    VoucherResponse create(VoucherRequest request);

    VoucherResponse update(Long id, VoucherRequest request);

    void delete(Long id);

    /**
     * Kiem tra va tinh so tien duoc giam, khong thay doi du lieu.
     */
    VoucherDiscountResult evaluate(String code, BigDecimal shippingFee, Long userId);

    /**
     * Ghi nhan luot su dung khi don hang duoc tao thanh cong.
     */
    void consume(VoucherDiscountResult result, Long userId, Long orderId);

    /**
     * Tra lai luot su dung khi don hang bi huy.
     */
    void release(Long orderId);
}
