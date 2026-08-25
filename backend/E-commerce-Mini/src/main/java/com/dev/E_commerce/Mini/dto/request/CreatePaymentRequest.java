package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.enums.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {
    PaymentMethod method;
}
