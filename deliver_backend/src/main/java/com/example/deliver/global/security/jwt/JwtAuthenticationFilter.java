package com.example.deliver.global.security.jwt;

import com.example.deliver.global.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //매 요청이 들어올 때마다 실행되어 토큰을 검사하여 인증 정보를 세팅한다.

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    //다음 요청들은 필터를 건너뛴다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/users/signup") //회원가입
                || uri.equals("/api/users/login") //로그인
                || uri.startsWith("/h2-console"); //H2 콘솔
    } //얘네는 로그인 전에도 접근 가능해야하니까, JWT 검사를 하지 않는다.

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");//Authorization 헤더인지 확인.
        //예를 들어, "Authorization: Bearer eyJ..." 이런 값을 읽는다.

        //Bearer 토큰인지 확인하고 토큰 추출.
        if (authHeader != null //헤더가 있어야 함.
                && authHeader.startsWith(BEARER_PREFIX) //Bearer 토큰이어야 함.
                && SecurityContextHolder.getContext().getAuthentication() == null) { //아직 인증 정보가 없어야 함.
            String token = authHeader.substring(BEARER_PREFIX.length()); //실제 토큰 문자열 부분만 추출. (Bearer eyJ...에서 "Bearer "를 제거한 "eyJ..." 부분만)

            //토큰 검증
            if (jwtTokenProvider.validateToken(token)) { //유효한 토큰이라면,
                String email = jwtTokenProvider.getEmail(token); //토큰으로 사용자 정보 추출 (이메일)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);//추출한 사용자 정보(이메일)로 사용자 조회.

                //Spring Security가 사용할 인증 객체 생성. 여기서 들어간 userDetails를 Controller에서 @AuthentiationPrinipal로 꺼내는 것.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //SecurityContext에 저장. 이 줄이 실행되면, Spring은 "이 요청은 인증된 사용자 요청이다"라고 이해한다.
                //그래서 이후 Controller에서 Authentication을 받을 수 있다.
                //이 Authentication으로 Controller에 있는 API 주소에 접근할 수 있게되는 것.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}