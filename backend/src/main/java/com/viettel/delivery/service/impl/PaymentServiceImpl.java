package com.viettel.delivery.service.impl;

import com.viettel.delivery.config.properties.VnPayProperties;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.PaymentStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.request.CreatePaymentRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.PaymentInitResponse;
import com.viettel.delivery.dto.response.PaymentResponse;
import com.viettel.delivery.dto.response.PaymentResultResponse;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Payment;
import com.viettel.delivery.entity.PaymentTransactionLog;
import com.viettel.delivery.event.PaymentCompletedEvent;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.PaymentMapper;
import com.viettel.delivery.model.PaymentCallbackResult;
import com.viettel.delivery.model.PaymentInitResult;
import com.viettel.delivery.repository.PaymentRepository;
import com.viettel.delivery.repository.PaymentTransactionLogRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.OrderService;
import com.viettel.delivery.service.PaymentService;
import com.viettel.delivery.service.payment.PaymentGateway;
import com.viettel.delivery.util.CodeGenerator;
import com.viettel.delivery.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_RETURN = "RETURN";
    private static final String ACTION_IPN = "IPN";
    private static final String ACTION_MOCK = "MOCK";

    private static final String IPN_CODE_SUCCESS = "00";
    private static final String IPN_CODE_ORDER_NOT_FOUND = "01";
    private static final String IPN_CODE_ALREADY_CONFIRMED = "02";
    private static final String IPN_CODE_INVALID_AMOUNT = "04";
    private static final String IPN_CODE_INVALID_SIGNATURE = "97";
    private static final String IPN_CODE_UNKNOWN_ERROR = "99";

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionLogRepository paymentLogRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final PaymentGateway vnPayGateway;
    private final VnPayProperties vnPayProperties;
    private final MessageUtil messageUtil;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentTransactionLogRepository paymentLogRepository,
                              PaymentMapper paymentMapper,
                              OrderService orderService,
                              List<PaymentGateway> gateways,
                              VnPayProperties vnPayProperties,
                              MessageUtil messageUtil,
                              ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentLogRepository = paymentLogRepository;
        this.paymentMapper = paymentMapper;
        this.orderService = orderService;
        this.vnPayGateway = gateways.stream()
                .filter(gateway -> PaymentMethod.VNPAY.equals(gateway.getMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chua dang ky cong thanh toan VNPay"));
        this.vnPayProperties = vnPayProperties;
        this.messageUtil = messageUtil;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public PaymentInitResponse createPayment(CreatePaymentRequest request, String clientIp) {
        Order order = orderService.getAccessibleOrder(request.getOrderId());

        if (!PaymentMethod.VNPAY.equals(order.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .txnRef(generateTxnRef())
                .order(order)
                .provider(PaymentMethod.VNPAY)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .ipAddress(clientIp)
                .orderInfo("Thanh toan don hang " + order.getOrderCode())
                .build());

        order.setPaymentStatus(PaymentStatus.PENDING);

        boolean mockMode = !vnPayGateway.isAvailable();
        String payUrl = mockMode
                ? buildMockPayUrl(payment)
                : initRealPayment(payment, clientIp, request.getBankCode());
        payment.setPayUrl(payUrl);

        return PaymentInitResponse.builder()
                .paymentId(payment.getId())
                .txnRef(payment.getTxnRef())
                .orderCode(order.getOrderCode())
                .amount(payment.getAmount())
                .payUrl(payUrl)
                .mockMode(mockMode)
                .build();
    }

    @Override
    @Transactional
    public PaymentResultResponse handleReturn(Map<String, String> params) {
        PaymentCallbackResult callback = vnPayGateway.parseCallback(params);
        Payment payment = findPaymentOrThrow(callback.txnRef());
        writeLog(payment, ACTION_RETURN, params.toString(), callback.responseCode(), null);

        if (!callback.validSignature()) {
            log.warn("Chu ky ReturnUrl khong hop le cho giao dich {}", callback.txnRef());
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_SIGNATURE);
        }

        applyCallbackResult(payment, callback);
        return buildResult(payment, callback.responseCode());
    }

    /**
     * IPN duoc goi server-to-server nen phai idempotent: goi lai nhieu lan khong duoc doi ket qua.
     */
    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        try {
            PaymentCallbackResult callback = vnPayGateway.parseCallback(params);
            if (!callback.validSignature()) {
                return ipnResponse(IPN_CODE_INVALID_SIGNATURE, "Invalid signature");
            }

            Payment payment = paymentRepository.findByTxnRefWithOrder(callback.txnRef()).orElse(null);
            if (payment == null) {
                return ipnResponse(IPN_CODE_ORDER_NOT_FOUND, "Order not found");
            }
            writeLog(payment, ACTION_IPN, params.toString(), callback.responseCode(), null);

            if (payment.isFinalized()) {
                return ipnResponse(IPN_CODE_ALREADY_CONFIRMED, "Order already confirmed");
            }
            if (callback.amount() != null && payment.getAmount().compareTo(callback.amount()) != 0) {
                return ipnResponse(IPN_CODE_INVALID_AMOUNT, "Invalid amount");
            }

            applyCallbackResult(payment, callback);
            return ipnResponse(IPN_CODE_SUCCESS, "Confirm Success");
        } catch (Exception ex) {
            log.error("Xu ly IPN that bai", ex);
            return ipnResponse(IPN_CODE_UNKNOWN_ERROR, "Unknown error");
        }
    }

    @Override
    @Transactional
    public PaymentResultResponse completeMockPayment(String txnRef, boolean success) {
        if (vnPayGateway.isAvailable()) {
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        Payment payment = findPaymentOrThrow(txnRef);
        if (payment.isFinalized()) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        PaymentCallbackResult callback = new PaymentCallbackResult(true, success, txnRef,
                "MOCK" + System.currentTimeMillis(), success ? "00" : "24",
                payment.getAmount(), "MOCKBANK", "MOCK", null);
        writeLog(payment, ACTION_MOCK, "mock=" + success, callback.responseCode(), null);
        applyCallbackResult(payment, callback);
        return buildResult(payment, callback.responseCode());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> search(BaseSearchRequest request) {
        Page<Payment> page = SecurityUtil.canViewAllData()
                ? paymentRepository.findAllWithOrder(request.toUnsortedPageable())
                : paymentRepository.findAllByCustomer(SecurityUtil.getCurrentUserId(), request.toUnsortedPageable());
        return PageResponse.of(page, paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByOrder(Long orderId) {
        orderService.getAccessibleOrder(orderId);
        return paymentMapper.toResponseList(paymentRepository.findByOrderId(orderId));
    }

    private String initRealPayment(Payment payment, String clientIp, String bankCode) {
        PaymentInitResult result = vnPayGateway.createPaymentUrl(payment, clientIp, bankCode);
        writeLog(payment, ACTION_CREATE, result.rawRequest(), null, "Tao URL thanh toan");
        return result.payUrl();
    }

    /**
     * Khi chua co TmnCode/HashSecret, he thong tro ve mot man hinh gia lap tren frontend
     * de quy trinh demo van chay tron ven.
     */
    private String buildMockPayUrl(Payment payment) {
        log.warn("Chua cau hinh VNPay sandbox, su dung che do thanh toan gia lap cho giao dich {}",
                payment.getTxnRef());
        return "%s?mock=true&txnRef=%s&amount=%s&orderCode=%s".formatted(
                vnPayProperties.returnUrl(),
                payment.getTxnRef(),
                payment.getAmount().toPlainString(),
                payment.getOrder().getOrderCode());
    }

    private void applyCallbackResult(Payment payment, PaymentCallbackResult callback) {
        if (payment.isFinalized()) {
            return;
        }

        payment.setTransactionNo(callback.transactionNo());
        payment.setBankCode(callback.bankCode());
        payment.setCardType(callback.cardType());
        payment.setResponseCode(callback.responseCode());
        payment.setPayDate(callback.payDate());

        Order order = payment.getOrder();
        if (callback.success()) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
            log.info("Don hang {} da thanh toan thanh cong {} VND", order.getOrderCode(), payment.getAmount());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            log.warn("Thanh toan don hang {} that bai, ma loi {}", order.getOrderCode(), callback.responseCode());
        }

        eventPublisher.publishEvent(new PaymentCompletedEvent(order.getId(), order.getOrderCode(),
                order.getCustomer().getId(), payment.getAmount(), callback.success()));
    }

    private PaymentResultResponse buildResult(Payment payment, String responseCode) {
        boolean success = PaymentStatus.PAID.equals(payment.getStatus());
        return PaymentResultResponse.builder()
                .success(success)
                .txnRef(payment.getTxnRef())
                .orderId(payment.getOrder().getId())
                .orderCode(payment.getOrder().getOrderCode())
                .amount(payment.getAmount())
                .responseCode(responseCode)
                .message(messageUtil.get(success
                        ? "notify.payment.success.title"
                        : "notify.payment.failed.title"))
                .build();
    }

    private Payment findPaymentOrThrow(String txnRef) {
        return paymentRepository.findByTxnRefWithOrder(txnRef)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private void writeLog(Payment payment, String action, String data, String responseCode, String message) {
        paymentLogRepository.save(PaymentTransactionLog.builder()
                .payment(payment)
                .action(action)
                .requestData(ACTION_CREATE.equals(action) ? data : null)
                .responseData(ACTION_CREATE.equals(action) ? null : data)
                .responseCode(responseCode)
                .message(message)
                .build());
    }

    private Map<String, String> ipnResponse(String code, String message) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("RspCode", code);
        response.put("Message", message);
        return response;
    }

    private String generateTxnRef() {
        String txnRef = CodeGenerator.paymentTxnRef();
        while (paymentRepository.findByTxnRefWithOrder(txnRef).isPresent()) {
            txnRef = CodeGenerator.paymentTxnRef();
        }
        return txnRef;
    }
}
