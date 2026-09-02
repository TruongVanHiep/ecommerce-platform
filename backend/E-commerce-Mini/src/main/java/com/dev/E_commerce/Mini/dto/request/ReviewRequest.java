package com.dev.E_commerce.Mini.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequest {
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    Long productId;

    Long orderItemId;

    // Trước đây rating nhận mọi số nguyên, kể cả số âm hoặc 9999.
    @Min(value = 1, message = "RATING_INVALID")
    @Max(value = 5, message = "RATING_INVALID")
    int rating;

    // Khớp với @Column(length = 2000) của entity Review, tránh lỗi 500 ở tầng DB.
    @Size(max = 2000, message = "COMMENT_INVALID")
    String comment;
}
