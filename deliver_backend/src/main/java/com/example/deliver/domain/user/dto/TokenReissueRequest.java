package com.example.deliver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest( //토큰 재발급 요청 dto
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}