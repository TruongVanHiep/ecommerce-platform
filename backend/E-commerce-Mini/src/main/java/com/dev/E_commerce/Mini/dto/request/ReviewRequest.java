package com.dev.E_commerce.Mini.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequest {
    Long productId;
    Long orderItemId;
    int rating;
    String comment;
}
