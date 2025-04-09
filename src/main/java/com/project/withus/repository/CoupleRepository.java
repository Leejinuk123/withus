package com.project.withus.repository;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByUser1OrUser2(User user1, User user2);
}
