package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.UserCreateRequest;
import com.viettel.delivery.dto.request.UserUpdateRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.dto.search.UserSearchRequest;

public interface UserService {

    PageResponse<UserResponse> search(UserSearchRequest request);

    UserResponse getById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);

    void resetPassword(Long id, String newPassword);
}
