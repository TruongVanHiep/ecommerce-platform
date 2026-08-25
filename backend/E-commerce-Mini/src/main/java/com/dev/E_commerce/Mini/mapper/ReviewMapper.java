package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.response.ReviewResponse;
import com.dev.E_commerce.Mini.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userFullName", source = "user.fullName")
    ReviewResponse toReviewResponse(Review review);
}
