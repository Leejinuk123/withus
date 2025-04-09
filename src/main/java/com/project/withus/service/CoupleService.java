package com.project.withus.service;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.user.User;
import com.project.withus.repository.CoupleRepository;
import com.project.withus.repository.UserRepository;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    public Couple createCouple(User me, String partnerInviteCode) {
        Optional<User> partnerOpt = userRepository.findByInviteCode(partnerInviteCode);
        if (partnerOpt.isEmpty()) {
            throw new RuntimeException("초대코드가 유효하지 않아요!");
        }

        User partner = partnerOpt.get();

        // 자기 자신 연결 방지
        if (me.getId().equals(partner.getId())) {
            throw new RuntimeException("자기 자신과는 연결할 수 없어요!");
        }

        // 이미 커플인지 확인
        if (getMyCouple(me).isPresent() || getMyCouple(partner).isPresent()) {
            throw new RuntimeException("이미 커플이에요!");
        }

        Couple couple = Couple.builder()
                .user1(me)
                .user2(partner)
                .startDate(LocalDate.now())
                .build();

        return coupleRepository.save(couple);
    }

    public Optional<Couple> getMyCouple(User user) {
        return coupleRepository.findByUser1OrUser2(user, user);
    }

    public User getPartner(User me, Couple couple) {
        return Objects.equals(me.getId(), couple.getUser1().getId())
                ? couple.getUser2()
                : couple.getUser1();
    }

    public long getDDay(User me) {
        return getMyCouple(me)
                .map(Couple::getStartDate)
                .map(start -> java.time.temporal.ChronoUnit.DAYS.between(start, LocalDate.now()))
                .orElse(0L);
    }

    public boolean isNotCoupled(User user) {
        return getMyCouple(user).isEmpty();
    }

    // 시작일 업데이트
    @Transactional
    public void updateStartDate(User user, LocalDate startDate) {
        Couple couple = getMyCouple(user).orElseThrow(() -> new RuntimeException("커플이 연결되지 않았어요!"));;
        if (couple == null) {
            throw new RuntimeException("커플 정보가 없습니다");
        }
        couple.updateStartDate(startDate);
    }

    // D-day 계산 (메인 페이지 등에서 사용)
    public String calculateDday(User user) {
        Couple couple = getMyCouple(user).orElseThrow(() -> new RuntimeException("커플이 연결되지 않았어요!"));
        if (couple == null || couple.getStartDate() == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(couple.getStartDate(), today);

        return "D+" + daysBetween;
    }

}
