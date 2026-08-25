package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.enums.PaymentMethod;
import com.dev.E_commerce.Mini.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Long id;
    Long orderId;
    PaymentMethod method;
    PaymentStatus status;
    BigDecimal amount;
    String transactionId;
    LocalDateTime paidAt;
    LocalDateTime createdAt;
}
