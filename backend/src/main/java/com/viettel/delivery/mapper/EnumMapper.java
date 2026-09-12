package com.viettel.delivery.mapper;

import com.viettel.delivery.constant.enums.AssignmentStatus;
import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.constant.enums.DiscountType;
import com.viettel.delivery.constant.enums.NotificationType;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.PaymentStatus;
import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.constant.enums.TrackingEventType;
import com.viettel.delivery.constant.enums.UserStatus;
import com.viettel.delivery.constant.enums.VehicleType;
import com.viettel.delivery.constant.enums.VoucherStatus;
import com.viettel.delivery.dto.response.EnumResponse;
import org.mapstruct.Mapper;

/**
 * Chuyen enum sang EnumResponse (code + description) cho moi mapper khac su dung.
 */
@Mapper(componentModel = "spring")
public interface EnumMapper {

    default EnumResponse map(UserStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(OrderStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(ServiceType value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(PaymentMethod value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(PaymentStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(ShipperStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(VehicleType value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(AssignmentStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(DiscountType value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(VoucherStatus value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(NotificationType value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(TrackingEventType value) {
        return EnumResponse.of(value);
    }

    default EnumResponse map(CodSettlementStatus value) {
        return EnumResponse.of(value);
    }
}
