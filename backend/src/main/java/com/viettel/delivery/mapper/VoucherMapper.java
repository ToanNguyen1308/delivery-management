package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.request.VoucherRequest;
import com.viettel.delivery.dto.response.VoucherResponse;
import com.viettel.delivery.entity.Voucher;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface VoucherMapper {

    VoucherResponse toResponse(Voucher voucher);

    List<VoucherResponse> toResponseList(List<Voucher> vouchers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget Voucher voucher, VoucherRequest request);
}
