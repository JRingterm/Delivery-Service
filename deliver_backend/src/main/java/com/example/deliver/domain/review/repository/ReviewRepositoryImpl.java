package com.example.deliver.domain.review.repository;

import com.example.deliver.domain.review.dto.ReviewSearchCondition;
import com.example.deliver.domain.review.entity.QReview;
import com.example.deliver.domain.review.entity.Review;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
//구현체. Spring Data JPA 규정상 "ReviewRepository + Impl" 로 이름을 맞추면 자동으로 커스텀 구현체로 연결된다.
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    //QuerydslConfig.java에서 등록해놓은 Bean 주입.
    private final JPAQueryFactory queryFactory;

    //Querydsl은 엔티티를 기반으로 Q클래스를 자동 생성한다.
    //Gradle 새로고침 하고, clean build 실행해야 Q클래스가 생성됨.
    //이걸 사용하면 문자열 쿼리가 아닌, 자바 코드로 안전하게 조건을 작성할 수 있다. (자바 코드라 컴파일 단계에서 오류 확인 가능.)
    private static final QReview review = QReview.review;

    @Override
    public Page<Review> searchStoreReviews(Long storeId, ReviewSearchCondition condition, Pageable pageable) {

        //리뷰 목록 조회 쿼리. QueryDSL의 where()은 조건이 null이면 무시한다.
        List<Review> content = queryFactory
                .selectFrom(review)
                //성능 개선을 위한 fetch join.
                .leftJoin(review.order).fetchJoin()
                .leftJoin(review.store).fetchJoin()
                .leftJoin(review.customer).fetchJoin()
                .where(
                        review.store.id.eq(storeId),
                        minRatingGoe(condition), //만약 요청에 minRating이 없다면, null 반환.
                        maxRatingLoe(condition),
                        contentContains(condition)
                )
                //정렬
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                //페이징 처리
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //count 쿼리. 페이징 응답에는 전체 개수가 필요하다.
        //count는 개수만 필요하므로, 연관 엔티티 로딩 불필요.
        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.store.id.eq(storeId),
                        minRatingGoe(condition),
                        maxRatingLoe(condition),
                        contentContains(condition)
                )
                .fetchOne();

        //Querydsl은 직접 Page를 반환하지 않기 때문에, PageImpl로 감싸준다.
        //결과적으로 Service에서는 Page<Review>로 받을 수 있다.
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    //minimum rating greater or equal. 최소 평점 이상 조건.
    private BooleanExpression minRatingGoe(ReviewSearchCondition condition) {
        //조건이 존재할 때만 where 추가.
        return condition != null && condition.minRating() != null
                ? review.rating.goe(condition.minRating())
                : null;
    }
    //maximum rating less or equal. 최대 평점 이하 조건.
    private BooleanExpression maxRatingLoe(ReviewSearchCondition condition) {
        return condition != null && condition.maxRating() != null
                ? review.rating.loe(condition.maxRating())
                : null;
    }

    //리뷰 내용에 키워드 검색 조건.
    private BooleanExpression contentContains(ReviewSearchCondition condition) {
        return condition != null && StringUtils.hasText(condition.keyword())
                ? review.content.contains(condition.keyword())
                : null;
    }

    //정렬 조건.
    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        //정렬 조건이 없다면, 내림차순(최신순) 정렬.
        if (pageable.getSort().isUnsorted()) {
            orders.add(review.id.desc());
            return orders;
        }

        for (Sort.Order sortOrder : pageable.getSort()) {
            boolean isAsc = sortOrder.isAscending();

            switch (sortOrder.getProperty()) {
                case "id" -> orders.add(isAsc ? review.id.asc() : review.id.desc());
                case "rating" -> orders.add(isAsc ? review.rating.asc() : review.rating.desc());
                default -> orders.add(review.id.desc()); //매핑된 것 외의 문자열이 들어오면 id,desc 정렬.
            }
        }

        return orders;
    }
}