package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long orderId;
    Long userId;
    BigDecimal totalPrice;
    BigDecimal discountAmount;
    BigDecimal shippingFee;
    String voucherCode;
    String status;
    String shippingAddress;
    String phone;
    LocalDateTime createdAt;
    List<OrderItemResponse> orderItemResponses;
}
