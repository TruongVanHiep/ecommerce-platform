package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {
    Long cartItemId;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Positive(message = "REQUIRED_FIELD_MISSING")
    Long productId;

    @Min(value = 1, message = "INVALID_QUANTITY")
    @Max(value = 1000, message = "INVALID_QUANTITY")
    int quantity;

    // CÁC TRƯỜNG DƯỚI ĐÂY SERVER KHÔNG DÙNG — giá luôn lấy từ Product trong DB
    // (xem CartService.addItemToCart). Giữ lại để tương thích payload cũ của
    // frontend; client gửi giá nào cũng không ảnh hưởng số tiền thực tế.
    String productName;
    String image;
    BigDecimal unitPrice;
    BigDecimal subTotal;
}
