package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.CartResponse;
import com.dev.E_commerce.Mini.entity.Cart;
import com.dev.E_commerce.Mini.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = CartItemMapper.class
)
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalItems", expression = "java(getTotalItems(cart.getCartItems()))")
    @Mapping(target = "totalPrice", expression = "java(getTotalPrice(cart.getCartItems()))")
    CartResponse toCartResponse(Cart cart);

    default Integer getTotalItems(List<CartItem> items){
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal getTotalPrice(List<CartItem> items){
        return items.stream()
                .map(i -> i.getUnitPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
