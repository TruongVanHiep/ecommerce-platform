package com.dev.E_commerce.Mini.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    String receiverName;
    String phone;
    String addressLine;
    boolean isDefault;
}
