package com.viettel.delivery.service.payment;

import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.entity.Payment;
import com.viettel.delivery.model.PaymentCallbackResult;
import com.viettel.delivery.model.PaymentInitResult;

import java.util.Map;

/**
 * Truu tuong hoa cong thanh toan. Muon them MoMo/ZaloPay chi can bo sung mot implementation moi.
 */
public interface PaymentGateway {

    PaymentMethod getMethod();

    boolean isAvailable();

    PaymentInitResult createPaymentUrl(Payment payment, String clientIp, String bankCode);

    PaymentCallbackResult parseCallback(Map<String, String> params);
}
