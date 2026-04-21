package com.example.deliver.domain.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuCreateRequest(
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자를 초과할 수 없습니다.")
        String name,

        @NotNull(message = "메뉴 가격은 필수입니다.")
        @Min(value = 0, message = "메뉴 가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotBlank(message = "메뉴 설명은 필수입니다.")
        @Size(max = 500, message = "메뉴 설명은 500자를 초과할 수 없습니다.")
        String description
) {
}