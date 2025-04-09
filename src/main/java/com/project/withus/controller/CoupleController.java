package com.project.withus.controller;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.user.CustomUserDetails;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/couple")
public class CoupleController {

    private final CoupleService coupleService;
    private final UserRepository userRepository;

    @GetMapping("/invite")
    public String invitePage() {
        return "couple/invite";
    }

    @PostMapping("/invite")
    public String invite(@AuthenticationPrincipal Object principal,
            @RequestParam String partnerNickname,
            Model model) {

        User me = null;

        // 1. 카카오 로그인 유저 처리
        if (principal instanceof OAuth2User oauthUser) {
            String myNickname = ((Map<String, Object>) ((Map<String, Object>) oauthUser.getAttributes().get("kakao_account"))
                    .get("profile")).get("nickname").toString();
            me = userRepository.findByNickname(myNickname).orElseThrow();
        }

        // 2. 일반 로그인 유저 처리
        else if (principal instanceof CustomUserDetails userDetails) {
            me = userDetails.getUser();
        }

        if (me == null) {
            model.addAttribute("error", "로그인 상태를 확인할 수 없습니다.");
            return "redirect:/login";
        }

        try {
            coupleService.createCouple(me, partnerNickname);
            model.addAttribute("message", "커플 연결 성공! ❤️");
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("alert", e.getMessage());
            return "couple/invite"; // 실패하면 다시 폼으로
        }
    }

    // D-day 설정 처리
    @PostMapping("/dday-setting")
    public String saveDdaySetting(@AuthenticationPrincipal Object principal,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            Model model) {

        User user = extractUser(principal);
        coupleService.updateStartDate(user, startDate);

        model.addAttribute("message", "D-day가 설정되었습니다");
        return "redirect:/";
    }

    // ✅ 유저 추출 공통 메서드
    @SuppressWarnings("unchecked")
    private User extractUser(Object principal) {
        if (principal instanceof OAuth2User oauthUser) {
            String nickname = ((Map<String, Object>) ((Map<String, Object>) oauthUser.getAttributes().get("kakao_account"))
                    .get("profile")).get("nickname").toString();
            return userRepository.findByNickname(nickname).orElseThrow();
        } else if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        } else {
            throw new IllegalStateException("로그인 상태가 아닙니다.");
        }
    }

}
