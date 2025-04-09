package com.project.withus.controller;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.letter.Letter;
import com.project.withus.domain.user.CustomUserDetails;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import com.project.withus.service.LetterService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/letters")
public class LetterController {

    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final LetterService letterService;

    // 📬 받은 편지 목록
    @GetMapping("/received")
    public String receivedLetters(@AuthenticationPrincipal Object principal, Model model, HttpServletRequest request) {
        User user = extractUser(principal);

        model.addAttribute("currentURI", request.getRequestURI()); // 👈 추가!

        if (coupleService.isNotCoupled(user)) {
            return "redirect:/?noCouple=true";
        }

        List<Letter> letters = letterService.getReceivedLetters(user);
        model.addAttribute("receivedLetters", letters);
        return "letters/received";
    }

    // 📤 보낸 편지 목록
    @GetMapping("/sent")
    public String sentLetters(@AuthenticationPrincipal Object principal, Model model, HttpServletRequest request) {
        User user = extractUser(principal);

        model.addAttribute("currentURI", request.getRequestURI()); // 👈 추가!

        if (coupleService.isNotCoupled(user)) {
            return "redirect:/?noCouple=true";
        }

        List<Letter> letters = letterService.getSentLetters(user);
        model.addAttribute("sentLetters", letters);
        model.addAttribute("type", "sent");

        return "letters/sent";
    }

    // 💌 편지 쓰기 페이지
    @GetMapping("/write")
    public String writeLetterForm(@AuthenticationPrincipal Object principal, Model model, HttpServletRequest request) {
        User user = extractUser(principal);
        model.addAttribute("currentURI", request.getRequestURI()); // 👈 추가!

        if (coupleService.isNotCoupled(user)) {
            return "redirect:/?noCouple=true";
        }
        return "letters/write";
    }

    // 💌 편지 전송
    @PostMapping("/send")
    public String sendLetter(@AuthenticationPrincipal Object principal,
            @RequestParam String content,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime unlockDate) {

        User me = extractUser(principal);
        Couple couple = coupleService.getMyCouple(me).orElseThrow(() -> new RuntimeException("커플이 연결되지 않았어요!"));
        User partner = coupleService.getPartner(me, couple);

        letterService.sendLetter(me, partner, content, unlockDate);
        return "redirect:/letters/sent";
    }

    // 👀 편지 읽기
    @GetMapping("/view/{id}")
    public String readLetter(@AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {
        User user = extractUser(principal);
        model.addAttribute("currentURI", request.getRequestURI());

        try {
            Letter letter = letterService.readLetter(user, id);

            // 잠금 여부 판단 (받는 사람만 제한)
            boolean isLocked = user.getId().equals(letter.getReceiver().getId()) &&
                    letter.getUnlockDate() != null &&
                    letter.getUnlockDate().isAfter(LocalDateTime.now());

            // 로그 출력
            System.out.println("읽은 편지 ID: " + letter.getId() +
                    ", 보낸이: " + letter.getSender().getId() +
                    ", 받는이: " + letter.getReceiver().getId());

            model.addAttribute("letter", letter);
            model.addAttribute("locked", isLocked); // 🔒 잠금 여부 전달

            return "letters/view";

        } catch (RuntimeException e) {
            List<Letter> letters = letterService.getReceivedLetters(user);
            model.addAttribute("receivedLetters", letters);
            model.addAttribute("errorMessage", e.getMessage());
            return "letters/received";
        }
    }

    // 🗑 편지 삭제 - 삭제 주체에 따라 리다이렉트 경로 분기
    @PostMapping("/{id}/delete")
    public String deleteLetter(@AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {
        User user = extractUser(principal);
        String currentURI = request.getRequestURI();

        try {
            boolean isSender = letterService.deleteLetter(user, id);
            return isSender ? "redirect:/letters/sent" : "redirect:/letters/received";

        } catch (RuntimeException e) {
            Letter letter = letterService.findById(id);
            model.addAttribute("letter", letter);
            model.addAttribute("currentURI", currentURI);
            model.addAttribute("errorMessage", e.getMessage());
            return "letters/view";
        }
    }

    // ✅ 유저 추출 공통 메서드
    @SuppressWarnings("unchecked")
    private User extractUser(Object principal) {
        if (principal instanceof OAuth2User oauthUser) {
            String oauthId = String.valueOf(oauthUser.getAttributes().get("id"));
            return userRepository.findByOauthId(oauthId)
                    .orElseThrow(() -> new IllegalStateException("소셜 로그인 유저를 찾을 수 없습니다."));
        } else if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        } else {
            throw new IllegalStateException("로그인 상태가 아닙니다.");
        }
    }

}
