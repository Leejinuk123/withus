package com.project.withus.repository;

import com.project.withus.domain.letter.Letter;
import com.project.withus.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    // 받은 편지 목록 (삭제되지 않은 것만)
    List<Letter> findAllByReceiverAndReceiverDeletedFalse(User receiver);

    // 보낸 편지 목록 (삭제되지 않은 것만)
    List<Letter> findAllBySenderAndSenderDeletedFalse(User sender);

    long countByReceiverAndIsReadFalseAndReceiverDeletedFalse(User receiver);

}
