package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.OrderItemResponse;
import com.dev.E_commerce.Mini.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subTotal",
            expression = "java(orderItem.getUnitPrice().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}