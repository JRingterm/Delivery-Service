package com.example.deliver.domain.payment.dto;

import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentMethod;
import com.example.deliver.domain.payment.entity.PaymentStatus;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long customerId,
        Integer amount,
        PaymentStatus status,
        PaymentMethod method
) {
    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getCustomer().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod()
        );
    }
}