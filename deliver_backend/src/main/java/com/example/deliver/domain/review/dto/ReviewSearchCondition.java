package com.example.deliver.domain.review.dto;

public record ReviewSearchCondition( //검색 조건 DTO
        Integer minRating, //최소 평점 조건
        Integer maxRating, //최대 평점 조건
        String keyword     //키워드 조건
) {
}
// Controller에서 @ModelAttribute ReviewSearchCondition condition 으로 받기 때문에,
// 요청 파라미터가 자동으로 매핑된다.
// 예를들어
// GET /api/stores/1/reviews?minRating=4&keyword=맛있
// 이런 식으로 들어오면,
// new ReviewSearchCondition(4, null, "맛있") 처럼 들어온다.