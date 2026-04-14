package com.example.deliver.domain.user.dto;

import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;

//회원가입 시 응답해주는 DTO
public record SignUpResponse(
        Long id,
        String email,
        String nickname,
        UserRole role
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}