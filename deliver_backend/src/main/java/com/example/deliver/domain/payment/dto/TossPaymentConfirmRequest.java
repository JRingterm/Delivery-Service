package com.example.deliver.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossPaymentConfirmRequest( //TOSS 결제 승인 DTO
        @NotBlank(message = "결제 키는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        Integer amount
) {
}