package com.viettel.delivery.service;

import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.CodSettlementResponse;
import com.viettel.delivery.dto.response.CodWalletResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.entity.Order;

import java.util.List;

public interface CodSettlementService {

    /**
     * Tao khoan doi soat khi shipper giao thanh cong mot don COD.
     */
    void createOnDelivered(Order order);

    CodWalletResponse getMyWallet();

    PageResponse<CodSettlementResponse> searchMySettlements(BaseSearchRequest request,
                                                            List<CodSettlementStatus> statuses);

    PageResponse<CodSettlementResponse> search(BaseSearchRequest request, List<CodSettlementStatus> statuses);

    /**
     * Shipper nop toan bo tien dang giu.
     */
    int submitAllHolding();

    CodSettlementResponse confirm(Long settlementId, String note);
}
