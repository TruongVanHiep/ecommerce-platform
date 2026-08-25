package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.OrderRequest;
import com.dev.E_commerce.Mini.dto.response.OrderResponse;
import com.dev.E_commerce.Mini.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    // Request -> Entity
    Order toOrder(OrderRequest request);

    // Entity -> Response
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "voucherCode", source = "voucher.code")
    @Mapping(target = "orderItemResponses", source = "orderItems")
    OrderResponse toOrderResponse(Order order);
}
