package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.dto.request.OrderCreateRequest;
import com.viettel.delivery.dto.response.ExcelImportResponse;
import com.viettel.delivery.dto.response.OrderResponse;
import com.viettel.delivery.dto.response.OrderSummaryResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.OrderExcelService;
import com.viettel.delivery.service.OrderService;
import com.viettel.delivery.util.ExcelUtil;
import com.viettel.delivery.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Import va export don hang bang Apache POI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExcelServiceImpl implements OrderExcelService {

    private static final String EXPORT_SHEET_NAME = "DanhSachDonHang";
    private static final String TEMPLATE_SHEET_NAME = "MauImportDonHang";

    private static final String[] EXPORT_HEADERS = {
            "STT", "Mã vận đơn", "Khách hàng", "Người nhận", "SĐT người nhận", "Địa chỉ giao",
            "Tỉnh/Thành", "Loại dịch vụ", "Trạng thái", "Thanh toán", "Trạng thái thanh toán",
            "Phí vận chuyển", "Tiền thu hộ", "Tổng tiền", "Shipper", "Hạn giao", "Ngày tạo"
    };

    private static final String[] IMPORT_HEADERS = {
            "Tên người gửi*", "SĐT người gửi*", "Địa chỉ lấy hàng*", "Quận/Huyện lấy", "Tỉnh/Thành lấy",
            "Tên người nhận*", "SĐT người nhận*", "Địa chỉ giao hàng*", "Quận/Huyện giao", "Tỉnh/Thành giao",
            "Khối lượng (kg)*", "Loại dịch vụ*", "Phương thức thanh toán*", "Tiền thu hộ",
            "Mô tả hàng hóa", "Ghi chú", "Tên đăng nhập khách hàng"
    };

    private static final String[] TEMPLATE_SAMPLE_ROW = {
            "Nguyễn Văn A", "0912345678", "144 Xuân Thủy, Cầu Giấy", "Cầu Giấy", "Hà Nội",
            "Trần Thị B", "0987654321", "25 Lý Thường Kiệt, Hoàn Kiếm", "Hoàn Kiếm", "Hà Nội",
            "2.5", "STANDARD", "COD", "500000", "Quần áo", "Giao giờ hành chính", "customer01"
    };

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportOrders(OrderSearchRequest request) {
        request.setSize(AppConstants.MAX_PAGE_SIZE);
        PageResponse<OrderSummaryResponse> page = orderService.search(request);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(EXPORT_SHEET_NAME);
            writeHeader(workbook, sheet, EXPORT_HEADERS);

            int rowIndex = 1;
            for (OrderSummaryResponse order : page.getContent()) {
                Row row = sheet.createRow(rowIndex);
                int column = 0;
                row.createCell(column++).setCellValue(rowIndex);
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getOrderCode()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getCustomerName()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getReceiverName()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getReceiverPhone()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getDeliveryAddress()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getDeliveryProvince()));
                row.createCell(column++).setCellValue(describe(order.getServiceType()));
                row.createCell(column++).setCellValue(describe(order.getStatus()));
                row.createCell(column++).setCellValue(describe(order.getPaymentMethod()));
                row.createCell(column++).setCellValue(describe(order.getPaymentStatus()));
                row.createCell(column++).setCellValue(ExcelUtil.toDouble(order.getShippingFee()));
                row.createCell(column++).setCellValue(ExcelUtil.toDouble(order.getCodAmount()));
                row.createCell(column++).setCellValue(ExcelUtil.toDouble(order.getTotalAmount()));
                row.createCell(column++).setCellValue(ExcelUtil.safe(order.getShipperName()));
                row.createCell(column++).setCellValue(ExcelUtil.format(order.getExpectedDeliveryAt()));
                row.createCell(column).setCellValue(ExcelUtil.format(order.getCreatedDate()));
                rowIndex++;
            }

            autoSize(sheet, EXPORT_HEADERS.length);
            workbook.write(outputStream);
            log.info("Da xuat {} don hang ra file Excel", page.getContent().size());
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Xuat file Excel that bai", ex);
            throw new BusinessException(ErrorCode.EXCEL_EXPORT_FAILED);
        }
    }

    @Override
    public byte[] buildImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
            writeHeader(workbook, sheet, IMPORT_HEADERS);

            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < TEMPLATE_SAMPLE_ROW.length; i++) {
                sampleRow.createCell(i).setCellValue(TEMPLATE_SAMPLE_ROW[i]);
            }

            Row noteRow = sheet.createRow(3);
            noteRow.createCell(0).setCellValue(
                    "Loại dịch vụ: STANDARD | EXPRESS | SAME_DAY. Phương thức thanh toán: COD | VNPAY. "
                            + "Cột có dấu * là bắt buộc. Xóa dòng ví dụ trước khi import.");

            autoSize(sheet, IMPORT_HEADERS.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Tao file mau that bai", ex);
            throw new BusinessException(ErrorCode.EXCEL_EXPORT_FAILED);
        }
    }

    /**
     * Khong bao transaction o day de moi dong duoc commit doc lap: mot dong loi
     * khong lam mat cac dong da import thanh cong truoc do.
     */
    @Override
    public ExcelImportResponse importOrders(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_EMPTY);
        }

        List<String> createdOrderCodes = new ArrayList<>();
        List<ExcelImportResponse.RowError> errors = new ArrayList<>();
        int totalRows = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > AppConstants.EXCEL_MAX_IMPORT_ROWS) {
                throw new BusinessException(ErrorCode.EXCEL_TOO_MANY_ROWS);
            }

            for (int rowIndex = AppConstants.EXCEL_HEADER_ROW_INDEX + 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (ExcelUtil.isEmptyRow(row, IMPORT_HEADERS.length)) {
                    continue;
                }
                totalRows++;
                try {
                    OrderResponse created = importSingleRow(row);
                    createdOrderCodes.add(created.getOrderCode());
                } catch (BusinessException ex) {
                    errors.add(buildRowError(rowIndex, messageUtil.get(ex.getErrorCode(), ex.getArgs())));
                } catch (Exception ex) {
                    log.warn("Loi import dong {}: {}", rowIndex + 1, ex.getMessage());
                    errors.add(buildRowError(rowIndex, ex.getMessage()));
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            log.error("Doc file Excel that bai", ex);
            throw new BusinessException(ErrorCode.EXCEL_INVALID_FORMAT);
        }

        if (totalRows == 0) {
            throw new BusinessException(ErrorCode.EXCEL_EMPTY);
        }

        log.info("Import don hang: {} dong, thanh cong {}, loi {}", totalRows, createdOrderCodes.size(), errors.size());
        return ExcelImportResponse.builder()
                .totalRows(totalRows)
                .successCount(createdOrderCodes.size())
                .failedCount(errors.size())
                .createdOrderCodes(createdOrderCodes)
                .errors(errors)
                .build();
    }

    private OrderResponse importSingleRow(Row row) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setSenderName(requireCell(row, 0, "Tên người gửi"));
        request.setSenderPhone(requireCell(row, 1, "SĐT người gửi"));
        request.setPickupAddress(requireCell(row, 2, "Địa chỉ lấy hàng"));
        request.setPickupDistrict(ExcelUtil.getString(row, 3));
        request.setPickupProvince(ExcelUtil.getString(row, 4));
        request.setReceiverName(requireCell(row, 5, "Tên người nhận"));
        request.setReceiverPhone(requireCell(row, 6, "SĐT người nhận"));
        request.setDeliveryAddress(requireCell(row, 7, "Địa chỉ giao hàng"));
        request.setDeliveryDistrict(ExcelUtil.getString(row, 8));
        request.setDeliveryProvince(ExcelUtil.getString(row, 9));

        BigDecimal weight = ExcelUtil.getBigDecimal(row, 10);
        if (weight == null || weight.signum() <= 0) {
            throw new IllegalArgumentException("Khối lượng không hợp lệ");
        }
        request.setWeightKg(weight);
        request.setServiceType(parseEnum(ServiceType.class, requireCell(row, 11, "Loại dịch vụ")));
        request.setPaymentMethod(parseEnum(PaymentMethod.class, requireCell(row, 12, "Phương thức thanh toán")));
        request.setCodAmount(ExcelUtil.getBigDecimal(row, 13));
        request.setPackageDescription(ExcelUtil.getString(row, 14));
        request.setNote(ExcelUtil.getString(row, 15));

        Long customerId = resolveCustomerId(ExcelUtil.getString(row, 16));
        return orderService.createForCustomer(request, customerId);
    }

    private Long resolveCustomerId(String username) {
        if (!StringUtils.hasText(username)) {
            return SecurityUtil.getCurrentUserId();
        }
        return userRepository.findByUsernameAndIsDeletedFalse(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng: " + username))
                .getId();
    }

    private String requireCell(Row row, int index, String fieldName) {
        String value = ExcelUtil.getString(row, index);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Thiếu dữ liệu bắt buộc: " + fieldName);
        }
        return value;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + value);
        }
    }

    private ExcelImportResponse.RowError buildRowError(int rowIndex, String message) {
        return ExcelImportResponse.RowError.builder()
                .rowNumber(rowIndex + 1)
                .message(message)
                .build();
    }

    private void writeHeader(Workbook workbook, Sheet sheet, String[] headers) {
        CellStyle style = ExcelUtil.headerStyle(workbook);
        Row headerRow = sheet.createRow(AppConstants.EXCEL_HEADER_ROW_INDEX);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
            headerRow.getCell(i).setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String describe(com.viettel.delivery.dto.response.EnumResponse value) {
        return value == null ? "" : value.getDescription();
    }
}
