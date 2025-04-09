package com.project.withus.repository;

import com.project.withus.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname); // 닉네임 중복 체크용

    boolean existsByNickname(String nickname);
}
