package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCartItemRequest {
    @Min(value = 1, message = "INVALID_QUANTITY")
    @Max(value = 1000, message = "INVALID_QUANTITY")
    int quantity;
}
