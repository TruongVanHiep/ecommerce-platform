package com.dev.E_commerce.Mini.dto.request;

import com.dev.E_commerce.Mini.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderStatusRequest {
    Status status;
}
