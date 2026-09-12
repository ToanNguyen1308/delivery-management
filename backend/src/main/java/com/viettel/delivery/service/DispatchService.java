package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.AssignmentStatus;
import com.viettel.delivery.dto.request.AssignOrderRequest;
import com.viettel.delivery.dto.request.AssignmentResponseRequest;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.AssignmentResponse;
import com.viettel.delivery.dto.response.PageResponse;

import java.util.List;

public interface DispatchService {

    /**
     * Gan don cho shipper. Neu request khong chi dinh shipper thi he thong tu chon
     * theo chien luoc cau hinh trong bien moi truong DISPATCH_STRATEGY.
     */
    AssignmentResponse assign(AssignOrderRequest request);

    AssignmentResponse respond(Long assignmentId, AssignmentResponseRequest request);

    PageResponse<AssignmentResponse> getMyAssignments(BaseSearchRequest request, List<AssignmentStatus> statuses);

    List<AssignmentResponse> getAssignmentsByOrder(Long orderId);
}
