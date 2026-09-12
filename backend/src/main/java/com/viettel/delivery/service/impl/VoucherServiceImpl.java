package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.VoucherStatus;
import com.viettel.delivery.dto.request.VoucherRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.VoucherResponse;
import com.viettel.delivery.dto.search.VoucherSearchRequest;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.entity.Voucher;
import com.viettel.delivery.entity.VoucherUsage;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.VoucherMapper;
import com.viettel.delivery.model.VoucherDiscountResult;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.repository.VoucherRepository;
import com.viettel.delivery.repository.VoucherUsageRepository;
import com.viettel.delivery.repository.specification.VoucherSpecification;
import com.viettel.delivery.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final BigDecimal PERCENT_BASE = new BigDecimal("100");

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoucherResponse> search(VoucherSearchRequest request) {
        Page<Voucher> page = voucherRepository.findAll(VoucherSpecification.build(request), request.toPageable());
        return PageResponse.of(page, voucherMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getById(Long id) {
        return voucherMapper.toResponse(findVoucherOrThrow(id));
    }

    @Override
    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        if (voucherRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new BusinessException(ErrorCode.VOUCHER_CODE_EXISTED);
        }
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .name(request.getName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .quantity(request.getQuantity())
                .usedCount(0)
                .usageLimitPerUser(request.getUsageLimitPerUser() != null ? request.getUsageLimitPerUser() : 1)
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .status(request.getStatus() != null ? request.getStatus() : VoucherStatus.ACTIVE)
                .description(request.getDescription())
                .build();
        Voucher saved = voucherRepository.save(voucher);
        log.info("Da tao voucher {}", saved.getCode());
        return voucherMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VoucherResponse update(Long id, VoucherRequest request) {
        Voucher voucher = findVoucherOrThrow(id);
        if (!voucher.getCode().equals(request.getCode())
                && voucherRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new BusinessException(ErrorCode.VOUCHER_CODE_EXISTED);
        }
        voucherMapper.updateEntity(voucher, request);
        return voucherMapper.toResponse(voucher);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Voucher voucher = findVoucherOrThrow(id);
        voucher.markDeleted();
        voucher.setStatus(VoucherStatus.INACTIVE);
        log.info("Da xoa voucher {}", voucher.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDiscountResult evaluate(String code, BigDecimal shippingFee, Long userId) {
        if (!StringUtils.hasText(code)) {
            return VoucherDiscountResult.none();
        }

        Voucher voucher = voucherRepository.findByCodeAndIsDeletedFalse(code.trim().toUpperCase()).orElse(null);
        if (voucher == null) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_NOT_FOUND);
        }
        if (!VoucherStatus.ACTIVE.equals(voucher.getStatus())) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_INACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getValidFrom())) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_NOT_STARTED);
        }
        if (now.isAfter(voucher.getValidTo())) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_EXPIRED);
        }
        if (!voucher.hasRemainingQuantity()) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_OUT_OF_QUANTITY);
        }
        if (shippingFee.compareTo(voucher.getMinOrderAmount()) < 0) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
        }
        if (userId != null
                && voucherUsageRepository.countByVoucherAndUser(voucher.getId(), userId)
                >= voucher.getUsageLimitPerUser()) {
            return VoucherDiscountResult.rejected(ErrorCode.VOUCHER_ALREADY_USED);
        }

        return VoucherDiscountResult.applied(calculateDiscount(voucher, shippingFee), voucher);
    }

    @Override
    @Transactional
    public void consume(VoucherDiscountResult result, Long userId, Long orderId) {
        if (result == null || !result.applied()) {
            return;
        }

        // Nap lai trong transaction hien tai de @Version phat huy tac dung chong tru trung luot
        Voucher voucher = voucherRepository.findByIdAndIsDeletedFalse(result.voucher().getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        if (!voucher.hasRemainingQuantity()) {
            throw new BusinessException(ErrorCode.VOUCHER_OUT_OF_QUANTITY);
        }
        voucher.increaseUsedCount();

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        voucherUsageRepository.save(VoucherUsage.builder()
                .voucher(voucher)
                .user(user)
                .orderId(orderId)
                .discountAmount(result.discountAmount())
                .usedAt(LocalDateTime.now())
                .build());
        log.info("Don hang {} da su dung voucher {} giam {}", orderId, voucher.getCode(), result.discountAmount());
    }

    @Override
    @Transactional
    public void release(Long orderId) {
        voucherUsageRepository.findByOrderId(orderId).ifPresent(usage -> {
            usage.getVoucher().decreaseUsedCount();
            usage.markDeleted();
            log.info("Da hoan tra luot dung voucher {} cua don hang {}", usage.getVoucher().getCode(), orderId);
        });
    }

    /**
     * Moi hinh thuc giam gia co cach tinh rieng, mo rong them chi can bo sung nhanh switch.
     */
    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal shippingFee) {
        BigDecimal discount = switch (voucher.getDiscountType()) {
            case PERCENT -> shippingFee.multiply(voucher.getDiscountValue())
                    .divide(PERCENT_BASE, AppConstants.MONEY_SCALE, RoundingMode.HALF_UP);
            case FIXED -> voucher.getDiscountValue();
            case FREE_SHIP -> shippingFee;
        };

        if (voucher.getMaxDiscountAmount() != null
                && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discount = voucher.getMaxDiscountAmount();
        }
        if (discount.compareTo(shippingFee) > 0) {
            discount = shippingFee;
        }
        return discount.setScale(AppConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private Voucher findVoucherOrThrow(Long id) {
        return voucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
    }
}
