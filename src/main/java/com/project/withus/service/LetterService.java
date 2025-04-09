package com.project.withus.service;

import com.project.withus.domain.letter.Letter;
import com.project.withus.domain.user.User;
import com.project.withus.repository.LetterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LetterService {

    private final LetterRepository letterRepository;

    @PersistenceContext
    private EntityManager entityManager; // EntityManager 주입

    // 편지 보내기
    @Transactional
    public Letter sendLetter(User sender, User receiver, String content, LocalDateTime unlockDate) {
        Letter letter = Letter.builder()
                .sender(sender)
                .receiver(receiver)
                .couple(null) // 나중에 설정
                .content(content)
                .createdAt(LocalDateTime.now())
                .unlockDate(unlockDate)
                .isRead(false)
                .senderDeleted(false)
                .receiverDeleted(false)
                .build();

        return letterRepository.save(letter);
    }

    // 편지 읽기 (unlock 날짜 체크 포함)
    @Transactional
    public Letter readLetter(User user, Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));

        Long userId = user.getId();
        Long senderId = letter.getSender().getId();
        Long receiverId = letter.getReceiver().getId();

        System.out.println("📨 Letter ID: " + letterId);
        System.out.println("✉ Sender: " + letter.getSender().getId());
        System.out.println("💌 Receiver: " + letter.getReceiver().getId());
        System.out.println("🙋‍♀️ Current User: " + user.getId());
        System.out.println("🔓 Unlock Date: " + letter.getUnlockDate());

        // 🔐 권한 확인: sender or receiver
        if (!userId.equals(senderId) && !userId.equals(receiverId)) {
            throw new RuntimeException("열람 권한이 없습니다.");
        }

        // 🔒 잠금 체크: 받은 사람만 제한
        if (userId.equals(receiverId)) {
            System.out.println("🔒 잠금 체크 대상: 받는 사람입니다.");
            LocalDateTime unlockDate = letter.getUnlockDate();
            if (unlockDate != null && unlockDate.isAfter(LocalDateTime.now())) {
                System.out.println("🚫 아직 열람할 수 없습니다.");
                throw new RuntimeException("아직 열람할 수 없는 편지입니다.");
            }
        }

        // ✅ 읽음 처리 (받은 사람이 처음 열람할 때만)
        if (!letter.isRead() && userId.equals(receiverId)) {
            letter.markAsRead(); // 예: letter.setRead(true);
        }

        return letter;
    }

    @Transactional
    public boolean deleteLetter(User user, Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));

        boolean isSender;

        if (user.getId().equals(letter.getSender().getId())) {
            isSender = true;
        } else if (user.getId().equals(letter.getReceiver().getId())) {
            isSender = false;
        } else {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        try {
            letterRepository.deleteById(letterId);
            letterRepository.flush();
        } catch (Exception e) {
            System.out.println("삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("편지 삭제 중 오류가 발생했습니다.", e);
        }

        return isSender;
    }



    // 받은 편지 목록
    public List<Letter> getReceivedLetters(User user) {
        return letterRepository.findAllByReceiverAndReceiverDeletedFalse(user);
    }

    // 보낸 편지 목록
    public List<Letter> getSentLetters(User user) {
        return letterRepository.findAllBySenderAndSenderDeletedFalse(user);
    }

    public long countUnreadLetters(User user) {
        return letterRepository.countByReceiverAndIsReadFalseAndReceiverDeletedFalse(user);
    }

    public Letter findById(Long id) {
        return letterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));
    }

}
