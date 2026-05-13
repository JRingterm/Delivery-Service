package com.example.deliver.domain.user.controller;

import com.example.deliver.domain.user.dto.*;
import com.example.deliver.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    //@RequestBody로 JSON -> Java 객체(SignUpRequest) 변환. @Valid로 DTO에 붙은 검증조건 확인.
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    //토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        return ResponseEntity.ok(userService.reissue(request));
    }
    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.logout(userDetails.getUsername()));
    }
}