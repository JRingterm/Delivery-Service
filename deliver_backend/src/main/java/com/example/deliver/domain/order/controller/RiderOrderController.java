package com.example.deliver.domain.order.controller;

import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider/orders")
@RequiredArgsConstructor
public class RiderOrderController { //라이더용 API

    private final OrderService orderService;

    //픽업
    @PatchMapping("/{orderId}/pickup")
    public ResponseEntity<OrderResponse> pickupOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.pickupOrder(userDetails.getUsername(), orderId));
    }
    //배달 완료
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.completeDelivery(userDetails.getUsername(), orderId));
    }
}