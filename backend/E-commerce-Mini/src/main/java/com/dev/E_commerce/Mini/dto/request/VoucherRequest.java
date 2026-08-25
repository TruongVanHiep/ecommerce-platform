package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.enums.DiscountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherRequest {
    String code;
    String description;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    Integer usageLimit;
    LocalDateTime startDate;
    LocalDateTime endDate;
    boolean active;
}
