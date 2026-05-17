package com.example.deliver.domain.order.controller;

import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.dto.OrderSearchCondition;
import com.example.deliver.domain.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/orders")
@RequiredArgsConstructor
public class OwnerOrderController { //점주(Owner) 전용

    private final OrderService orderService;

    //주문 목록 조회, Querydsl 적용.
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> findOwnerOrders(
            @AuthenticationPrincipal UserDetails userDetails,   //현재 로그인한 Owner
            @ModelAttribute OrderSearchCondition condition,     //검색 조건
            Pageable pageable                                   //페이지/정렬 조건.
            ) {
        return ResponseEntity.ok(orderService.findOwnerOrders(userDetails.getUsername(), condition, pageable));
    }
    //주문 단건 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOwnerOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.findOwnerOrder(userDetails.getUsername(), orderId));
    }
    //주문 수락
    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.acceptOrder(userDetails.getUsername(), orderId));
    }
    //주문 거절
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.rejectOrder(userDetails.getUsername(), orderId));
    }
    //조리 시작
    @PatchMapping("/{orderId}/cooking")
    public ResponseEntity<OrderResponse> startCooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.startCooking(userDetails.getUsername(), orderId));
    }
    //배달 준비 완료
    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<OrderResponse> markReadyForDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.markReadyForDelivery(userDetails.getUsername(), orderId));
    }
}