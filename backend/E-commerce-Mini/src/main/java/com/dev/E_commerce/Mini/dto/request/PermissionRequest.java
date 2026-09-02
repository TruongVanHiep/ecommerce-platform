package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
    @NotBlank(message = "NAME_INVALID")
    @Size(max = 50, message = "NAME_INVALID")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "NAME_INVALID")
    String name;

    @Size(max = 255, message = "DESCRIPTION_INVALID")
    String description;
}
