package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.ReviewRequest;
import com.dev.E_commerce.Mini.dto.response.ReviewResponse;
import com.dev.E_commerce.Mini.entity.OrderItem;
import com.dev.E_commerce.Mini.entity.Product;
import com.dev.E_commerce.Mini.entity.Review;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.ReviewMapper;
import com.dev.E_commerce.Mini.repository.OrderItemRepository;
import com.dev.E_commerce.Mini.repository.ProductRepository;
import com.dev.E_commerce.Mini.repository.ReviewRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {
    ReviewRepository reviewRepository;
    ProductRepository productRepository;
    OrderItemRepository orderItemRepository;
    UserRepository userRepository;
    ReviewMapper reviewMapper;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User user = currentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        OrderItem orderItem = null;
        if (request.getOrderItemId() != null) {
            orderItem = orderItemRepository.findByIdAndOrder_User_Id(request.getOrderItemId(), user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
            if (reviewRepository.existsByOrderItem_Id(orderItem.getId())) {
                throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTED);
            }
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse updateOwnReview(Long reviewId, ReviewRequest request) {
        User user = currentUser();
        Review review = reviewRepository.findByIdAndUser_Id(reviewId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteOwnReview(Long reviewId) {
        User user = currentUser();
        Review review = reviewRepository.findByIdAndUser_Id(reviewId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAllByProduct_Id(productId, pageable)
                .map(reviewMapper::toReviewResponse);
    }
}
