package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.ReviewRequest;
import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import com.dev.E_commerce.Mini.dto.response.ReviewResponse;
import com.dev.E_commerce.Mini.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@RequestBody ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> updateOwnReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateOwnReview(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOwnReview(@PathVariable Long id) {
        reviewService.deleteOwnReview(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<Page<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getReviewsByProduct(productId, page, size))
                .build();
    }
}
