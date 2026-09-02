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
public class AddressRequest {
    @NotBlank(message = "FULL_NAME_INVALID")
    @Size(max = 100, message = "FULL_NAME_INVALID")
    String receiverName;

    @NotBlank(message = "PHONE_INVALID")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "PHONE_INVALID")
    String phone;

    @NotBlank(message = "ADDRESS_INVALID")
    @Size(max = 500, message = "ADDRESS_INVALID")
    String addressLine;

    boolean isDefault;
}
