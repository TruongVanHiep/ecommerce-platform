package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Long cartItemId;
    Long productId;
    String productName;
    String image;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal subTotal;
}
