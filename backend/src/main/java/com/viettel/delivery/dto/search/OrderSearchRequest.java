package com.viettel.delivery.dto.search;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.PaymentStatus;
import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(name = "OrderSearchRequest", description = "Dieu kien tim kiem don hang")
public class OrderSearchRequest extends BaseSearchRequest {

    @Schema(description = "Tim theo ma van don, ten hoac so dien thoai nguoi nhan")
    private String keyword;

    @Schema(description = "Loc theo nhieu trang thai cung luc")
    private List<OrderStatus> statuses;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private ServiceType serviceType;

    private Long shipperId;

    @Schema(description = "Chi admin va dieu phoi vien duoc loc theo khach hang khac")
    private Long customerId;

    private String deliveryProvince;

    private String deliveryDistrict;

    @Schema(description = "Chi lay don da xac nhan nhung chua co shipper")
    private Boolean unassignedOnly;

    private LocalDate fromDate;

    private LocalDate toDate;
}
