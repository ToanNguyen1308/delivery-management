package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.UserStatus;
import com.viettel.delivery.dto.request.UserCreateRequest;
import com.viettel.delivery.dto.request.UserUpdateRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.dto.search.UserSearchRequest;
import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.UserMapper;
import com.viettel.delivery.repository.RoleGroupRepository;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.repository.specification.UserSpecification;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(UserSearchRequest request) {
        Page<User> page = userRepository.findAll(UserSpecification.build(request), request.toPageable());
        return PageResponse.of(page, userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTED);
        }
        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTED);
        }
        if (StringUtils.hasText(request.getPhoneNumber())
                && userRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_PHONE_EXISTED);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoleGroups(resolveRoleGroups(request.getRoleGroupIds()));

        User saved = userRepository.save(user);
        log.info("Da tao nguoi dung moi: {}", saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findUserOrThrow(id);

        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTED);
        }
        if (StringUtils.hasText(request.getPhoneNumber())
                && userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
            throw new BusinessException(ErrorCode.USER_PHONE_EXISTED);
        }

        userMapper.updateEntity(user, request);
        if (!CollectionUtils.isEmpty(request.getRoleGroupIds())) {
            user.setRoleGroups(resolveRoleGroups(request.getRoleGroupIds()));
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id.equals(SecurityUtil.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.USER_CANNOT_DELETE_SELF);
        }
        User user = findUserOrThrow(id);
        user.markDeleted();
        user.setStatus(UserStatus.INACTIVE);
        log.info("Da xoa mem nguoi dung: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = findUserOrThrow(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Da dat lai mat khau cho nguoi dung: {}", user.getUsername());
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findByIdWithAuthorities(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private Set<RoleGroup> resolveRoleGroups(Set<Long> roleGroupIds) {
        Set<RoleGroup> roleGroups = roleGroupRepository.findAllByIdInAndIsDeletedFalse(roleGroupIds);
        if (roleGroups.size() != roleGroupIds.size()) {
            throw new ResourceNotFoundException(ErrorCode.ROLE_GROUP_NOT_FOUND);
        }
        return roleGroups;
    }
}
