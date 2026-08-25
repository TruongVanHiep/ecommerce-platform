package com.dev.E_commerce.Mini.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    Long id;
    Long productId;
    Long userId;
    String username;
    String userFullName;
    int rating;
    String comment;
    LocalDateTime createdAt;
}
