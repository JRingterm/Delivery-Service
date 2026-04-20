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

    //회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        //받아온 데이터의 이메일, 닉네임 중복 확인.
        validateDuplicate(request);

        //entity에 만들어놓은 User Builder 사용하여 (DTO -> 엔티티) 만들기.
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))//비밀번호는 평문X. BCrypt로 암호화.
                .nickname(request.nickname())
                .role(UserRole.CUSTOMER) //회원가입 시 role을 CUSTOMER로 고정.
                .build();

        User savedUser = userRepository.save(user);
        return SignUpResponse.from(savedUser);//dto의 정적메소드 호출
    }

    //로그인
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try { //인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            ); //Spring Security에게 이메일과 비밀번호를 넘겨서 일치하는지 확인. 성공하면 로그인, 실패하면 예외 발생.
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        //인증 성공 후, DB에서 사용자 정보를 다시 가져온다.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        //JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        return LoginResponse.of(user, accessToken, jwtTokenProvider.getAccessTokenExpirationMs());
    }

    //이메일, 닉네임 중복 확인 및 방지. 중복시 409 CONFLICT
    private void validateDuplicate(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
    }
}