package com.example.deliver.domain.payment.dto;

import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentMethod;
import com.example.deliver.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long customerId,
        Integer amount,
        PaymentStatus status,
        PaymentMethod method,
        //프론트가 PG 결제가 언제 승인 됐는지, PG 주문 번호가 무엇인지 확인할 수 있도록 추가.
        String paymentKey,
        String pgOrderId,
        LocalDateTime approvedAt
) {
    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getCustomer().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getPaymentKey(),
                payment.getPgOrderId(),
                payment.getApprovedAt()
        );
    }
}