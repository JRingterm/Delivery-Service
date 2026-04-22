package com.example.deliver.global.security;

import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService { //로그인할 때 DB에서 사용자 찾기.

    private final UserRepository userRepository;

    //Spring Security는 로그인할 때 사용자 정보를 UserDetailsService를 통해 찾는다.
    //사용자 조회 후, Spring Security가 이해할 수 있는 UserDetails 형태로 변환.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) //이 프로젝트에서 username은 이메일.
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}