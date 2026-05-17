package com.example.deliver.domain.order.dto;

import com.example.deliver.domain.order.entity.OrderStatus;

public record OrderSearchCondition( //Owner 주문 목록 조회시 검색 조건을 담는 DTO
        OrderStatus status,     //주문 상태
        Integer minTotalPrice,  //검색할 최소 금액 필터
        Integer maxTotalPrice   //검색할 최대 금액 필터
) {
}