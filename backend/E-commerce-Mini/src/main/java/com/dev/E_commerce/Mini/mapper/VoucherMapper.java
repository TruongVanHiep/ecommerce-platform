package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.VoucherRequest;
import com.dev.E_commerce.Mini.dto.response.VoucherResponse;
import com.dev.E_commerce.Mini.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    Voucher toVoucher(VoucherRequest request);
    VoucherResponse toVoucherResponse(Voucher voucher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    void updateVoucher(@MappingTarget Voucher voucher, VoucherRequest request);
}
