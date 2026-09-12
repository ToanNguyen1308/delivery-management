package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.enums.ServiceType;
import org.springframework.stereotype.Component;

/**
 * Giao tieu chuan: chi ap dung cong thuc co ban, khong co phu phi uu tien.
 */
@Component
public class StandardShippingFeeStrategy extends AbstractShippingFeeStrategy {

    @Override
    public ServiceType getServiceType() {
        return ServiceType.STANDARD;
    }
}
