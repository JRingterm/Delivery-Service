package com.example.deliver.domain.payment.dto;

public record TossPaymentConfirmResponse(
        String paymentKey,
        String orderId,
        String method,
        Integer totalAmount,
        String status,
        String approvedAt
) {
}