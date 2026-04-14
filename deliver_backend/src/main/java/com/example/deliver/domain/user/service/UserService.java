package com.example.deliver.domain.user.service;

import com.example.deliver.domain.user.dto.LoginRequest;
import com.example.deliver.domain.user.dto.LoginResponse;
import com.example.deliver.domain.user.dto.SignUpRequest;
import com.example.deliver.domain.user.dto.SignUpResponse;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import com.example.deliver.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateDuplicate(request);

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(UserRole.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        return SignUpResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        return LoginResponse.of(user, accessToken, jwtTokenProvider.getAccessTokenExpirationMs());
    }

    private void validateDuplicate(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
    }
}