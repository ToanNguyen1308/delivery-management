package com.viettel.delivery.dto.request;

import com.viettel.delivery.constant.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * Lop cha cho moi DTO tim kiem. Dieu kien filter cua tung man hinh se ke thua lop nay.
 */
@Getter
@Setter
@Schema(description = "Tham so phan trang va sap xep dung chung")
public abstract class BaseSearchRequest implements Serializable {

    @Min(0)
    @Schema(description = "Trang bat dau tu 0", example = "0")
    private Integer page = AppConstants.DEFAULT_PAGE;

    @Min(1)
    @Max(AppConstants.MAX_PAGE_SIZE)
    @Schema(description = "So ban ghi moi trang", example = "10")
    private Integer size = AppConstants.DEFAULT_PAGE_SIZE;

    @Schema(description = "Truong sap xep", example = "createdDate")
    private String sortBy = AppConstants.DEFAULT_SORT_FIELD;

    @Schema(description = "Chieu sap xep: ASC hoac DESC", example = "DESC")
    private String sortDirection = AppConstants.SORT_DESC;

    public Pageable toPageable() {
        int pageNumber = page == null || page < 0 ? AppConstants.DEFAULT_PAGE : page;
        int pageSize = size == null || size < 1 ? AppConstants.DEFAULT_PAGE_SIZE : Math.min(size, AppConstants.MAX_PAGE_SIZE);
        String field = StringUtils.hasText(sortBy) ? sortBy : AppConstants.DEFAULT_SORT_FIELD;
        Sort.Direction direction = AppConstants.SORT_DESC.equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, field));
    }

    /**
     * Dung cho cac truy van JPQL tu viet, khong ap dung sort cua Pageable vao entity graph.
     */
    public Pageable toUnsortedPageable() {
        int pageNumber = page == null || page < 0 ? AppConstants.DEFAULT_PAGE : page;
        int pageSize = size == null || size < 1 ? AppConstants.DEFAULT_PAGE_SIZE : Math.min(size, AppConstants.MAX_PAGE_SIZE);
        return PageRequest.of(pageNumber, pageSize);
    }
}
