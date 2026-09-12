package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.request.ShipperCreateRequest;
import com.viettel.delivery.dto.request.ShipperUpdateRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.ShipperPerformanceResponse;
import com.viettel.delivery.dto.response.ShipperResponse;
import com.viettel.delivery.dto.search.ShipperSearchRequest;
import com.viettel.delivery.entity.Shipper;

import java.util.List;

public interface ShipperService {

    PageResponse<ShipperResponse> search(ShipperSearchRequest request);

    ShipperResponse getById(Long id);

    ShipperResponse getMyProfile();

    ShipperResponse create(ShipperCreateRequest request);

    ShipperResponse update(Long id, ShipperUpdateRequest request);

    void delete(Long id);

    ShipperResponse updateMyStatus(ShipperStatus status);

    List<ShipperResponse> getAvailableShippers();

    ShipperPerformanceResponse getPerformance(Long shipperId);

    ShipperPerformanceResponse getMyPerformance();

    /**
     * Dung noi bo cho cac service khac (dieu phoi, tracking, doi soat COD).
     */
    Shipper getShipperEntityByUserId(Long userId);
}
