package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.entity.CartItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long cartId;
    Long userId;
    BigDecimal totalPrice;
    Integer totalItems;
    List<CartItemResponse> items;
    LocalDateTime createdAt;
}
