package com.example.deliver.domain.payment.dto;

import com.example.deliver.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
        @NotNull(message = "결제 금액은 필수입니다.")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        Integer amount,
        @NotNull PaymentMethod method
) {
}