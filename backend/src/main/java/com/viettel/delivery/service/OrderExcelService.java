package com.viettel.delivery.service;

import com.viettel.delivery.dto.response.ExcelImportResponse;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import org.springframework.web.multipart.MultipartFile;

public interface OrderExcelService {

    byte[] exportOrders(OrderSearchRequest request);

    byte[] buildImportTemplate();

    ExcelImportResponse importOrders(MultipartFile file);
}
