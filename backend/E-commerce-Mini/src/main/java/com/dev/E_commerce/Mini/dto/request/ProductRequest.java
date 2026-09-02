package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "NAME_INVALID")
    @Size(max = 255, message = "NAME_INVALID")
    String name;

    // Khớp @Column(length = 1000) của entity Product.
    @Size(max = 1000, message = "DESCRIPTION_INVALID")
    String description;

    @NotNull(message = "PRICE_INVALID")
    @DecimalMin(value = "0.0", inclusive = false, message = "PRICE_INVALID")
    @Digits(integer = 13, fraction = 2, message = "PRICE_INVALID")
    BigDecimal price;

    // Chỉ chấp nhận URL http/https — chặn javascript: và data: URI bị lưu vào DB
    // rồi render ở client.
    @Pattern(regexp = "^$|^https?://.+", message = "IMAGE_URL_INVALID")
    @Size(max = 1000, message = "IMAGE_URL_INVALID")
    String image;

    @PositiveOrZero(message = "STOCK_INVALID")
    @Max(value = 1_000_000, message = "STOCK_INVALID")
    int stock;

    @NotNull(message = "REQUIRED_FIELD_MISSING")
    @Positive(message = "REQUIRED_FIELD_MISSING")
    Long categoryId;
}
