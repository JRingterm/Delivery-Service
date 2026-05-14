package com.example.deliver.domain.review.repository;

import com.example.deliver.domain.review.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    //리뷰 중복 작성 방지(1주문 1리뷰)
    boolean existsByOrderId(Long orderId);

    //가게 리뷰 조회. Pageable 추가.
    @EntityGraph(attributePaths = {"store", "customer", "order"})
    Page<Review> findAllByStoreId(Long storeId, Pageable pageable);

    //내(Customer)가 쓴 리뷰 조회
    @EntityGraph(attributePaths = {"store", "customer", "order"})
    List<Review> findAllByCustomerId(Long customerId);
}