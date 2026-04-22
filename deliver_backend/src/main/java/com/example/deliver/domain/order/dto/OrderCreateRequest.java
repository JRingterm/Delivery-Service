package com.example.deliver.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderCreateRequest(
        @NotNull(message = "가게 ID는 필수입니다.")
        Long storeId,

        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> items
) {
}