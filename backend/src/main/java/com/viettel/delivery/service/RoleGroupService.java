package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.RoleGroupRequest;
import com.viettel.delivery.dto.response.FunctionResponse;
import com.viettel.delivery.dto.response.RoleGroupResponse;

import java.util.List;

public interface RoleGroupService {

    List<RoleGroupResponse> getAll();

    RoleGroupResponse getById(Long id);

    List<FunctionResponse> getAllFunctions();

    RoleGroupResponse create(RoleGroupRequest request);

    RoleGroupResponse update(Long id, RoleGroupRequest request);

    void delete(Long id);
}
