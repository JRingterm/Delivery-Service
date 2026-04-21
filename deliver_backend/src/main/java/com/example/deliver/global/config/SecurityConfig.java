package com.example.deliver.global.config;

import com.example.deliver.global.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig { //보안 규칙을 정하는 클래스. Authorization(인가)규칙을 정한다.

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) //REST API 테스트를 쉽게 하기위해 CSRF 보호를 끈다.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //STATELESS. 세션 안쓰고 JWT로만 인증하겠다.
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() //postman 에서 403을 401로 출력하는 문제 발생으로 추가한 코드
                        //MenuService에서 403에러 발생. Spring이 에러 응답을 만들기 위해 내부적으로 /error 경로로 처리하려고 했는데, 그 /error 요청을 Security가 인증이 안됐다며 막아버림.
                        //따라서 Security를 넘지 못한 요청이라서 403이 아닌 401로 출력이된 것.
                        //로그 확인해가며 Security 넘어서 Controller, Service까지 들어온거 확인해서 403이 나와야할 것은 분명했는데, 이런 경우가 있었네...

                        .requestMatchers("/api/users/signup", "/api/users/login", "/h2-console/**", "/error").permitAll() //permitAll()로 열어두면 권한없이도 접속 가능.
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll() //가게의 조회나 메뉴 조회는 인증없어도 가능.
                        .anyRequest().authenticated() //나머지 요청은 인증(토큰) 필요.
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) //Spring Security의 authenticationEntryPoint() -> 인증이 안된 요청이 들어왔을 때, 어떻게 처리할지. 401
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN)) //Spring Security의 accessDeniedHandler() -> 인증은 되었는데 권한이 없는 경우. 403
                )
                .httpBasic(AbstractHttpConfigurer::disable) //브라우저 기본 로그인 창을 사용하지 않겠다.
                .formLogin(AbstractHttpConfigurer::disable) //Spring 기본 로그인 페이지를 사용하지 않겠다.
                //요청이 들어오면, Spring 기본 인증 필터보다 먼저 내가 만든 JwtAuthenticationFilter를 먼저 실행하라.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}