package com.dev.E_commerce.Mini.dto.response;

import com.dev.E_commerce.Mini.enums.DiscountType;
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
public class VoucherResponse {
    Long id;
    String code;
    String description;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    Integer usageLimit;
    int usedCount;
    LocalDateTime startDate;
    LocalDateTime endDate;
    boolean active;
}
