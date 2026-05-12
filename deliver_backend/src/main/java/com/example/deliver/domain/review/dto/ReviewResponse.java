package com.example.deliver.domain.review.dto;

import com.example.deliver.domain.review.entity.Review;

public record ReviewResponse(
        Long id,
        Long orderId,
        Long storeId,
        Long customerId,
        String customerNickname,
        Integer rating,
        String content
) {
    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getStore().getId(),
                review.getCustomer().getId(),
                review.getCustomer().getNickname(),
                review.getRating(),
                review.getContent()
        );
    }
}