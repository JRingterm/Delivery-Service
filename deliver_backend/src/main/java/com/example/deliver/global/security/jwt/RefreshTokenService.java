
package com.example.deliver.global.security.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService { //Redis에 Refresh 토큰을 저장, 조회, 삭제, 검증

    private static final String KEY_PREFIX = "RT:";

    private final StringRedisTemplate redisTemplate;

    //Redis에 토큰 저장
    public void saveRefreshToken(String email, String refreshToken, long ttlMs) {
        redisTemplate.opsForValue().set(generateKey(email), refreshToken, ttlMs, TimeUnit.MILLISECONDS);
    }
    //Redis에서 해당하는 사용자의 Refresh 토큰 조회
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(generateKey(email));
    }
    //로그아웃 시 사용. Redis에서 해당하는 사용자의 Refresh 토큰 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(generateKey(email));
    }
    //일치 여부 검증. 요청과 Redis의 Refresh 토큰 비교.
    public boolean isRefreshTokenMatched(String email, String refreshToken) {
        String saved = getRefreshToken(email);
        return saved != null && saved.equals(refreshToken);
    }

    private String generateKey(String email) {
        return KEY_PREFIX + email;
    }
}