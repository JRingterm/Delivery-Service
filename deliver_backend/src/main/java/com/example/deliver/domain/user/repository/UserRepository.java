package com.example.deliver.domain.user.repository;

import com.example.deliver.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    //이메일이 이미 존재하는지
    boolean existsByEmail(String email);
    //닉네임이 이미 존재하는지
    boolean existsByNickname(String nickname);
    //이메일로 유저 찾기
    Optional<User> findByEmail(String email);
}