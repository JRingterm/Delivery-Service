package com.example.deliver.domain.review.repository;

import com.example.deliver.domain.review.dto.ReviewSearchCondition;
import com.example.deliver.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

//기본 JpaRepository로 처리하기 어려운 복합 검색을 위한 커스텀 Repository 인터페이스. -> Querydsl로 하겠다.
public interface ReviewRepositoryCustom {

    Page<Review> searchStoreReviews(Long storeId, ReviewSearchCondition condition, Pageable pageable);

}