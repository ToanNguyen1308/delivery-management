package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.dto.request.RoleGroupRequest;
import com.viettel.delivery.dto.response.FunctionResponse;
import com.viettel.delivery.dto.response.RoleGroupResponse;
import com.viettel.delivery.entity.FunctionEntity;
import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.RoleGroupMapper;
import com.viettel.delivery.repository.FunctionRepository;
import com.viettel.delivery.repository.RoleGroupRepository;
import com.viettel.delivery.service.RoleGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleGroupServiceImpl implements RoleGroupService {

    private final RoleGroupRepository roleGroupRepository;
    private final FunctionRepository functionRepository;
    private final RoleGroupMapper roleGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleGroupResponse> getAll() {
        return roleGroupMapper.toResponseList(roleGroupRepository.findAllWithFunctions());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleGroupResponse getById(Long id) {
        return roleGroupMapper.toResponse(roleGroupRepository.findByIdWithFunctions(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_GROUP_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FunctionResponse> getAllFunctions() {
        return roleGroupMapper.toFunctionResponseList(
                functionRepository.findAllByIsDeletedFalseOrderByModuleAscFunctionCodeAsc());
    }

    @Override
    @Transactional
    public RoleGroupResponse create(RoleGroupRequest request) {
        if (roleGroupRepository.existsByRoleGroupCodeAndIsDeletedFalse(request.getRoleGroupCode())) {
            throw new BusinessException(ErrorCode.ROLE_GROUP_CODE_EXISTED);
        }
        RoleGroup roleGroup = RoleGroup.builder()
                .roleGroupCode(request.getRoleGroupCode())
                .roleGroupName(request.getRoleGroupName())
                .description(request.getDescription())
                .functions(resolveFunctions(request.getFunctionIds()))
                .build();
        RoleGroup saved = roleGroupRepository.save(roleGroup);
        log.info("Da tao nhom quyen: {}", saved.getRoleGroupCode());
        return roleGroupMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleGroupResponse update(Long id, RoleGroupRequest request) {
        RoleGroup roleGroup = roleGroupRepository.findByIdWithFunctions(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_GROUP_NOT_FOUND));

        if (!roleGroup.getRoleGroupCode().equals(request.getRoleGroupCode())
                && roleGroupRepository.existsByRoleGroupCodeAndIsDeletedFalse(request.getRoleGroupCode())) {
            throw new BusinessException(ErrorCode.ROLE_GROUP_CODE_EXISTED);
        }

        roleGroup.setRoleGroupCode(request.getRoleGroupCode());
        roleGroup.setRoleGroupName(request.getRoleGroupName());
        roleGroup.setDescription(request.getDescription());
        roleGroup.setFunctions(resolveFunctions(request.getFunctionIds()));
        return roleGroupMapper.toResponse(roleGroup);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RoleGroup roleGroup = roleGroupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_GROUP_NOT_FOUND));
        roleGroup.markDeleted();
        log.info("Da xoa nhom quyen: {}", roleGroup.getRoleGroupCode());
    }

    private Set<FunctionEntity> resolveFunctions(Set<Long> functionIds) {
        Set<FunctionEntity> functions = functionRepository.findAllByIdInAndIsDeletedFalse(functionIds);
        if (functions.size() != functionIds.size()) {
            throw new ResourceNotFoundException(ErrorCode.FUNCTION_NOT_FOUND);
        }
        return functions;
    }
}
