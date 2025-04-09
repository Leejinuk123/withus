package com.project.withus.domain.letter;

import com.project.withus.domain.user.User;
import com.project.withus.domain.couple.Couple;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Letter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    private User receiver;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private Couple couple;

    @Lob
    private String content;

    private boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime unlockDate; // null이면 잠금 없음

    private boolean senderDeleted;
    private boolean receiverDeleted;

    // ===== 도메인 로직 =====

    public boolean canSenderDelete() {
        return !isRead && !senderDeleted;
    }

    public boolean canReceiverDelete() {
        return isRead && !receiverDeleted;
    }

    public boolean isVisibleTo(User user) {
        if (user.equals(sender)) return !senderDeleted;
        if (user.equals(receiver)) return !receiverDeleted;
        return false;
    }

    public boolean canBeReadBy(User user) {
        if (!isVisibleTo(user)) return false;
        if (unlockDate == null) return true;
        return LocalDateTime.now().isAfter(unlockDate);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markSenderDeleted() {
        this.senderDeleted = true;
    }

    public void markReceiverDeleted() {
        this.receiverDeleted = true;
    }
}
