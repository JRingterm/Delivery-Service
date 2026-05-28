package com.example.deliver.domain.payment.controller;

import com.example.deliver.domain.payment.dto.PaymentCreateRequest;
import com.example.deliver.domain.payment.dto.PaymentResponse;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmRequest;
import com.example.deliver.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //MOCK 결제. 서버 내부에서 PAID 처리를 하며, 실제 PG 서버 호출은 없다. 즉, 테스트용
    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<PaymentResponse> payOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentResponse response = paymentService.payOrder(userDetails.getUsername(), orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //TOSS 결제 HTTP 요청 처리.
    @PostMapping("/payments/toss/confirm")
    public ResponseEntity<PaymentResponse> confirmTossPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TossPaymentConfirmRequest request
    ) {
        PaymentResponse response = paymentService.confirmTossPayment(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<PaymentResponse> findPaymentByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.findPaymentByOrder(userDetails.getUsername(), orderId));
    }
}