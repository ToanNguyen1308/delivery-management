package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.CodSettlementResponse;
import com.viettel.delivery.dto.response.CodWalletResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.entity.CodSettlement;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.CodSettlementMapper;
import com.viettel.delivery.repository.CodSettlementRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.CodSettlementService;
import com.viettel.delivery.service.ShipperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodSettlementServiceImpl implements CodSettlementService {

    private final CodSettlementRepository codSettlementRepository;
    private final CodSettlementMapper codSettlementMapper;
    private final ShipperService shipperService;

    @Override
    @Transactional
    public void createOnDelivered(Order order) {
        if (!order.isCodOrder() || order.getCurrentShipper() == null) {
            return;
        }
        if (codSettlementRepository.existsByOrderIdAndIsDeletedFalse(order.getId())) {
            return;
        }

        codSettlementRepository.save(CodSettlement.builder()
                .shipper(order.getCurrentShipper())
                .order(order)
                .amount(order.getCodAmount())
                .status(CodSettlementStatus.HOLDING)
                .collectedAt(LocalDateTime.now())
                .build());
        log.info("Tao khoan doi soat COD {} VND cho don {}", order.getCodAmount(), order.getOrderCode());
    }

    @Override
    @Transactional(readOnly = true)
    public CodWalletResponse getMyWallet() {
        Shipper shipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        return CodWalletResponse.builder()
                .shipperId(shipper.getId())
                .shipperCode(shipper.getShipperCode())
                .holdingAmount(sum(shipper.getId(), CodSettlementStatus.HOLDING))
                .submittedAmount(sum(shipper.getId(), CodSettlementStatus.SUBMITTED))
                .confirmedAmount(sum(shipper.getId(), CodSettlementStatus.CONFIRMED))
                .holdingCount(codSettlementRepository
                        .findByShipperAndStatus(shipper.getId(), CodSettlementStatus.HOLDING).size())
                .submittedCount(codSettlementRepository
                        .findByShipperAndStatus(shipper.getId(), CodSettlementStatus.SUBMITTED).size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CodSettlementResponse> searchMySettlements(BaseSearchRequest request,
                                                                    List<CodSettlementStatus> statuses) {
        Shipper shipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        Page<CodSettlement> page = codSettlementRepository.findByShipperAndStatuses(
                shipper.getId(), resolveStatuses(statuses), request.toUnsortedPageable());
        return PageResponse.of(page, codSettlementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CodSettlementResponse> search(BaseSearchRequest request,
                                                      List<CodSettlementStatus> statuses) {
        Page<CodSettlement> page = codSettlementRepository.findByStatuses(
                resolveStatuses(statuses), request.toUnsortedPageable());
        return PageResponse.of(page, codSettlementMapper::toResponse);
    }

    @Override
    @Transactional
    public int submitAllHolding() {
        Shipper shipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        List<CodSettlement> holdings =
                codSettlementRepository.findByShipperAndStatus(shipper.getId(), CodSettlementStatus.HOLDING);
        if (holdings.isEmpty()) {
            throw new BusinessException(ErrorCode.COD_NOTHING_TO_SETTLE);
        }

        LocalDateTime now = LocalDateTime.now();
        holdings.forEach(settlement -> {
            settlement.setStatus(CodSettlementStatus.SUBMITTED);
            settlement.setSubmittedAt(now);
            settlement.getOrder().setCodSettlementStatus(CodSettlementStatus.SUBMITTED);
        });

        log.info("Shipper {} da nop {} khoan COD", shipper.getShipperCode(), holdings.size());
        return holdings.size();
    }

    @Override
    @Transactional
    public CodSettlementResponse confirm(Long settlementId, String note) {
        CodSettlement settlement = codSettlementRepository.findByIdWithDetails(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COD_SETTLEMENT_NOT_FOUND));

        settlement.setStatus(CodSettlementStatus.CONFIRMED);
        settlement.setConfirmedAt(LocalDateTime.now());
        settlement.setConfirmedByUserId(SecurityUtil.getCurrentUserId());
        settlement.setNote(note);
        settlement.getOrder().setCodSettlementStatus(CodSettlementStatus.CONFIRMED);

        log.info("Da xac nhan doi soat COD cho don {}", settlement.getOrder().getOrderCode());
        return codSettlementMapper.toResponse(settlement);
    }

    private BigDecimal sum(Long shipperId, CodSettlementStatus status) {
        BigDecimal amount = codSettlementRepository.sumAmountByShipperAndStatus(shipperId, status);
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private List<CodSettlementStatus> resolveStatuses(List<CodSettlementStatus> statuses) {
        return CollectionUtils.isEmpty(statuses)
                ? List.copyOf(EnumSet.allOf(CodSettlementStatus.class))
                : statuses;
    }
}
