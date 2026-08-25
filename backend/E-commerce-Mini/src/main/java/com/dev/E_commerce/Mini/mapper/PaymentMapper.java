package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.PaymentResponse;
import com.dev.E_commerce.Mini.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toPaymentResponse(Payment payment);
}
