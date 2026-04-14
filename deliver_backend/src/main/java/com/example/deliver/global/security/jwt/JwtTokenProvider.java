package com.example.deliver.global.security.jwt;

import com.example.deliver.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider { //토큰 생성과 검증 담당.

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = jwtProperties.accessTokenExpirationMs();
    }

    //로그인 성공 후 호출되는 메소드. 토큰을 생성한다.
    public String createAccessToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(email) //"이 이메일을 가진 사용자가 로그인 했다" 는 정보를 담고있다.
                .issuedAt(now) //발급시간
                .expiration(expiry) //만료시간
                .signWith(signingKey)
                .compact();
    }

    //토큰에서 이메일 꺼내기
    public String getEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    //토큰이 유효한지 검증. 위조되었는지, 만료되었는지
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}