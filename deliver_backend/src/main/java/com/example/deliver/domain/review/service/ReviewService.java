package com.example.deliver.domain.review.service;

import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.review.dto.ReviewCreateRequest;
import com.example.deliver.domain.review.dto.ReviewResponse;
import com.example.deliver.domain.review.entity.Review;
import com.example.deliver.domain.review.repository.ReviewRepository;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    //리뷰 생성
    @Transactional
    public ReviewResponse createReview(String customerEmail, Long orderId, ReviewCreateRequest request) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 리뷰를 작성할 수 있습니다.");
        }

        //본인의 주문인지 확인
        Order order = orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "완료된 주문만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByOrderId(orderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 리뷰가 작성된 주문입니다.");
        }

        Review review = Review.builder()
                .order(order)
                .store(order.getStore())
                .customer(customer)
                .rating(request.rating())
                .content(request.content())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.toResponse(savedReview);
    }

    //내 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> findMyReviews(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 내 리뷰를 조회할 수 있습니다.");
        }

        return reviewRepository.findAllByCustomerId(customer.getId()).stream()
                .map(ReviewResponse::toResponse)
                .toList();
    }
    //가게 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> findStoreReviews(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");
        }

        return reviewRepository.findAllByStoreId(storeId).stream()
                .map(ReviewResponse::toResponse)
                .toList();
    }
}