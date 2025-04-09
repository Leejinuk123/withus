package com.project.withus.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String loginId; // 일반 로그인 사용자만 사용

    @Setter
    private String nickname;

    private String password;

    @Column(unique = true)
    private String oauthId; // 카카오에서 받은 고유 ID

    @Column(unique = true, updatable = false)
    private String inviteCode; // ✅ 초대코드

    @PrePersist
    public void generateInviteCode() {
        if (this.inviteCode == null) {
            this.inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}