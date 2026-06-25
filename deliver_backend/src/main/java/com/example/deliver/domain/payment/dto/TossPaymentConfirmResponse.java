package com.example.deliver.domain.payment.dto;

public record TossPaymentConfirmResponse( //Toss 응답 DTO
        String paymentKey,
        String orderId,
        String method,
        Integer totalAmount,
        String status,
        String approvedAt
) {
}