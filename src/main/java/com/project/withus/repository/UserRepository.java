package com.project.withus.repository;

import com.project.withus.domain.user.User;
import java.util.Optional;
import org.springframework.boot.autoconfigure.container.ContainerImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname); // 닉네임 중복 체크용

    boolean existsByNickname(String nickname);

    Optional<User> findByOauthId(String oauthId);

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByInviteCode(String partnerInviteCode);
}
