package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.enums.DiscountType;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "VOUCHER_CODE_INVALID")
    @Size(max = 50, message = "VOUCHER_CODE_INVALID")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "VOUCHER_CODE_INVALID")
    String code;

    @Size(max = 255, message = "DESCRIPTION_INVALID")
    String description;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    DiscountType discountType;

    @NotNull(message = "DISCOUNT_VALUE_INVALID")
    @DecimalMin(value = "0.0", inclusive = false, message = "DISCOUNT_VALUE_INVALID")
    @Digits(integer = 13, fraction = 2, message = "DISCOUNT_VALUE_INVALID")
    BigDecimal discountValue;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @DecimalMin(value = "0.0", message = "INVALID_INPUT")
    BigDecimal minOrderValue;

    @DecimalMin(value = "0.0", message = "INVALID_INPUT")
    BigDecimal maxDiscountAmount;

    @Positive(message = "INVALID_INPUT")
    Integer usageLimit;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    LocalDateTime startDate;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    LocalDateTime endDate;

    boolean active;
}
