package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherPreviewResponse {
    String code;
    BigDecimal discountAmount;
    BigDecimal finalTotal;
}
