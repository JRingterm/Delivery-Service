package com.example.deliver.domain.user.dto;

import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;

//로그인 시 응답해주는 DTO
public record LoginResponse(
        Long id,
        String email,
        String nickname,
        UserRole role,
        String tokenType,
        String accessToken,
        long expiresIn,
        String message
) {//정적 메소드. 객체생성 없이 클래스 이름으로 호출 가능. Entity -> DTO 변환 책임을 DTO가 갖게 하기 위함.
    public static LoginResponse of(User user, String accessToken, long expiresIn) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                "Bearer",
                accessToken,
                expiresIn,
                "로그인에 성공했습니다."
        );
    }
}