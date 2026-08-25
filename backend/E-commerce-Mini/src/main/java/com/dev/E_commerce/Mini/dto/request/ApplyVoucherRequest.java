package com.dev.E_commerce.Mini.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherRequest {
    String code;
    BigDecimal orderTotal;
}
