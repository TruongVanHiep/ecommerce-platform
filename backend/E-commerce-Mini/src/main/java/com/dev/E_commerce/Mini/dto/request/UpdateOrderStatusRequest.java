package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderStatusRequest {
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    Status status;
}
