package com.viettel.delivery.service.payment;

import com.viettel.delivery.config.properties.VnPayProperties;
import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.entity.Payment;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.model.PaymentCallbackResult;
import com.viettel.delivery.model.PaymentInitResult;
import com.viettel.delivery.util.VnPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tich hop cong thanh toan VNPay (sandbox), phien ban API 2.1.0, ky bang HMAC-SHA512.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VnPayGateway implements PaymentGateway {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    private final VnPayProperties vnPayProperties;

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public boolean isAvailable() {
        return vnPayProperties.isConfigured();
    }

    @Override
    public PaymentInitResult createPaymentUrl(Payment payment, String clientIp, String bankCode) {
        if (!isAvailable()) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_DISABLED);
        }

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayProperties.version());
        params.put("vnp_Command", vnPayProperties.command());
        params.put("vnp_TmnCode", vnPayProperties.tmnCode());
        params.put("vnp_Amount", toVnPayAmount(payment.getAmount())); // VNPay: số tiền × 100, không thập phân
        params.put("vnp_CurrCode", vnPayProperties.currencyCode());
        params.put("vnp_TxnRef", payment.getTxnRef());
        params.put("vnp_OrderInfo", payment.getOrderInfo());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnPayProperties.locale());
        params.put("vnp_ReturnUrl", vnPayProperties.returnUrl());
        params.put("vnp_IpAddr", StringUtils.hasText(clientIp) ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(PAYMENT_TIMEOUT_MINUTES).format(VNPAY_DATE_FORMAT));
        if (StringUtils.hasText(bankCode)) {
            params.put("vnp_BankCode", bankCode);
        }

        String hashData = VnPayUtil.buildHashData(params);
        String secureHash = VnPayUtil.hmacSha512(vnPayProperties.hashSecret(), hashData);
        String payUrl = "%s?%s&%s=%s".formatted(vnPayProperties.payUrl(), VnPayUtil.buildQueryUrl(params),
                VnPayUtil.SECURE_HASH_FIELD, secureHash);

        log.info("Tao URL thanh toan VNPay cho giao dich {}", payment.getTxnRef());
        return new PaymentInitResult(payUrl, hashData);
    }

    @Override
    public PaymentCallbackResult parseCallback(Map<String, String> rawParams) {
        Map<String, String> params = new HashMap<>(rawParams);
        String receivedHash = params.get(VnPayUtil.SECURE_HASH_FIELD);
        boolean validSignature = VnPayUtil.verifySignature(vnPayProperties.hashSecret(), params, receivedHash);

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        boolean success = SUCCESS_RESPONSE_CODE.equals(responseCode)
                && (transactionStatus == null || SUCCESS_RESPONSE_CODE.equals(transactionStatus));

        return new PaymentCallbackResult(
                validSignature,
                success,
                params.get("vnp_TxnRef"),
                params.get("vnp_TransactionNo"),
                responseCode,
                fromVnPayAmount(params.get("vnp_Amount")),
                params.get("vnp_BankCode"),
                params.get("vnp_CardType"),
                params.get("vnp_PayDate"));
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(AppConstants.VND_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private BigDecimal fromVnPayAmount(String rawAmount) {
        if (!StringUtils.hasText(rawAmount)) {
            return null;
        }
        try {
            return new BigDecimal(rawAmount).divide(AppConstants.VND_MULTIPLIER, 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            log.warn("So tien tra ve tu VNPay khong hop le: {}", rawAmount);
            return null;
        }
    }
}
