package com.example.deliver.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreCreateRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Size(max = 100, message = "가게 이름은 100자를 초과할 수 없습니다.")
        String name,

        @NotBlank(message = "가게 설명은 필수입니다.")
        @Size(max = 500, message = "가게 설명은 500자를 초과할 수 없습니다.")
        String description
) {
}