package com.example.deliver.domain.order.controller;

import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/orders")
@RequiredArgsConstructor
public class OwnerOrderController { //점주(Owner) 전용

    private final OrderService orderService;

    //주문 목록 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> findOwnerOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.findOwnerOrders(userDetails.getUsername()));
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
}