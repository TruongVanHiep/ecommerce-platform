package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.CartItemResponse;
import com.dev.E_commerce.Mini.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "image", source = "product.image")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subTotal",
            expression = "java(cartItem.getUnitPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemResponse toCartItemResponse(CartItem cartItem);
}


