package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "orderId", source = "order.id")
    // Thông tin chuyển khoản không nằm trong bảng payments — SepayService điền sau
    // khi map. Khai báo ignore để MapStruct không cảnh báo mỗi lần build.
    @Mapping(target = "bankCode", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "accountName", ignore = true)
    @Mapping(target = "transferContent", ignore = true)
    @Mapping(target = "qrUrl", ignore = true)
    PaymentResponse toPaymentResponse(Payment payment);
}
