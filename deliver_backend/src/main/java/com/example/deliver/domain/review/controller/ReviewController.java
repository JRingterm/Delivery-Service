package com.example.deliver.domain.review.controller;

import com.example.deliver.domain.review.dto.ReviewCreateRequest;
import com.example.deliver.domain.review.dto.ReviewResponse;
import com.example.deliver.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    //리뷰 작성. 리뷰는 주문에 속한 리소스.
    @PostMapping("/api/orders/{orderId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(userDetails.getUsername(), orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //내 리뷰 조회
    @GetMapping("/api/reviews/me")
    public ResponseEntity<List<ReviewResponse>> findMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.findMyReviews(userDetails.getUsername()));
    }
    //가게 리뷰 조회. Pageable 추가. Spring이 요청 파라미터를 자동으로 변경 ex>GET /api/stores/1/reviews?page=0&size=5
    @GetMapping("/api/stores/{storeId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> findStoreReviews(@PathVariable Long storeId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.findStoreReviews(storeId, pageable));
    }
}