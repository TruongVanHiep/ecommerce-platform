package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectRequest {
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @Size(max = 4096, message = "INVALID_INPUT")
    String token;
}
