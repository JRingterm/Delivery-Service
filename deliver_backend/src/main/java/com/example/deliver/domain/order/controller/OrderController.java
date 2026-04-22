package com.example.deliver.domain.order.controller;

import com.example.deliver.domain.order.dto.OrderCreateRequest;
import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //주문 생성
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderResponse response = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //주문 내역 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> findMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.findMyOrders(userDetails.getUsername()));
    }

    //주문 내역 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findMyOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.findMyOrder(userDetails.getUsername(), id));
    }
}