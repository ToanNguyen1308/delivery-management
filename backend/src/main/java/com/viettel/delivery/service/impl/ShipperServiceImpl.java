package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.request.ShipperCreateRequest;
import com.viettel.delivery.dto.request.ShipperUpdateRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.ShipperPerformanceResponse;
import com.viettel.delivery.dto.response.ShipperResponse;
import com.viettel.delivery.dto.search.ShipperSearchRequest;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.ShipperMapper;
import com.viettel.delivery.repository.ShipperRepository;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.repository.specification.ShipperSpecification;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.ShipperService;
import com.viettel.delivery.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    private final ShipperRepository shipperRepository;
    private final UserRepository userRepository;
    private final ShipperMapper shipperMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShipperResponse> search(ShipperSearchRequest request) {
        Page<Shipper> page = shipperRepository.findAll(ShipperSpecification.build(request), request.toPageable());
        return PageResponse.of(page, shipperMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperResponse getById(Long id) {
        return shipperMapper.toResponse(findShipperOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperResponse getMyProfile() {
        return shipperMapper.toResponse(getShipperEntityByUserId(SecurityUtil.getCurrentUserId()));
    }

    @Override
    @Transactional
    public ShipperResponse create(ShipperCreateRequest request) {
        if (shipperRepository.existsByUserIdAndIsDeletedFalse(request.getUserId())) {
            throw new BusinessException(ErrorCode.SHIPPER_USER_EXISTED);
        }
        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Shipper shipper = Shipper.builder()
                .shipperCode(generateShipperCode())
                .user(user)
                .vehicleType(request.getVehicleType())
                .licensePlate(request.getLicensePlate())
                .maxLoadKg(request.getMaxLoadKg() != null
                        ? request.getMaxLoadKg()
                        : request.getVehicleType().getDefaultCapacityKg())
                .zone(request.getZone())
                .status(ShipperStatus.OFFLINE)
                .maxConcurrentOrders(request.getMaxConcurrentOrders() != null ? request.getMaxConcurrentOrders() : 5)
                .idCardUrl(request.getIdCardUrl())
                .driverLicenseUrl(request.getDriverLicenseUrl())
                .note(request.getNote())
                .build();

        Shipper saved = shipperRepository.save(shipper);
        log.info("Da tao ho so shipper {} cho tai khoan {}", saved.getShipperCode(), user.getUsername());
        return shipperMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ShipperResponse update(Long id, ShipperUpdateRequest request) {
        Shipper shipper = findShipperOrThrow(id);
        shipperMapper.updateEntity(shipper, request);
        return shipperMapper.toResponse(shipper);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shipper shipper = findShipperOrThrow(id);
        shipper.markDeleted();
        shipper.setStatus(ShipperStatus.SUSPENDED);
        log.info("Da xoa ho so shipper {}", shipper.getShipperCode());
    }

    @Override
    @Transactional
    public ShipperResponse updateMyStatus(ShipperStatus status) {
        if (status == ShipperStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.SHIPPER_INVALID_STATUS);
        }
        Shipper shipper = getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        shipper.setStatus(status);
        log.info("Shipper {} chuyen sang trang thai {}", shipper.getShipperCode(), status);
        return shipperMapper.toResponse(shipper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipperResponse> getAvailableShippers() {
        return shipperMapper.toResponseList(
                shipperRepository.findAvailableShippers(EnumSet.of(ShipperStatus.ONLINE, ShipperStatus.BUSY)));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperPerformanceResponse getPerformance(Long shipperId) {
        return buildPerformance(findShipperOrThrow(shipperId));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperPerformanceResponse getMyPerformance() {
        return buildPerformance(getShipperEntityByUserId(SecurityUtil.getCurrentUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Shipper getShipperEntityByUserId(Long userId) {
        return shipperRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPER_PROFILE_MISSING));
    }

    private ShipperPerformanceResponse buildPerformance(Shipper shipper) {
        long delivered = shipper.getTotalDelivered() == null ? 0 : shipper.getTotalDelivered();
        long failed = shipper.getTotalFailed() == null ? 0 : shipper.getTotalFailed();
        long inProgress = shipper.getCurrentLoad() == null ? 0 : shipper.getCurrentLoad();
        long finished = delivered + failed;

        BigDecimal successRate = finished == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(delivered)
                        .multiply(PERCENT)
                        .divide(BigDecimal.valueOf(finished), 2, RoundingMode.HALF_UP);

        return ShipperPerformanceResponse.builder()
                .shipperId(shipper.getId())
                .shipperCode(shipper.getShipperCode())
                .fullName(shipper.getUser().getFullName())
                .totalAssigned(finished + inProgress)
                .totalDelivered(delivered)
                .totalFailed(failed)
                .inProgress(inProgress)
                .successRate(successRate)
                .rating(shipper.getRating())
                .build();
    }

    private Shipper findShipperOrThrow(Long id) {
        return shipperRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHIPPER_NOT_FOUND));
    }

    private String generateShipperCode() {
        long sequence = shipperRepository.findMaxId() + 1;
        String code = CodeGenerator.shipperCode(sequence);
        while (shipperRepository.existsByShipperCodeAndIsDeletedFalse(code)) {
            code = CodeGenerator.shipperCode(++sequence);
        }
        return code;
    }
}
