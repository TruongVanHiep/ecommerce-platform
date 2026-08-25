package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    Long id;
    String receiverName;
    String phone;
    String addressLine;
    boolean isDefault;
}
