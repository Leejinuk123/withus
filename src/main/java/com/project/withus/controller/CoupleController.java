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
    public String invitePage(@AuthenticationPrincipal Object principal, Model model) {
        User me = extractUser(principal);
        model.addAttribute("inviteCode", me.getInviteCode()); // ✅ 초대코드 전달
        return "couple/invite";
    }

    @PostMapping("/invite")
    public String invite(@AuthenticationPrincipal Object principal,
            @RequestParam String partnerInviteCode,
            Model model) {

        User me = null;

        // 1. 카카오 로그인 유저 처리 (✅ oauthId 기준 조회)
        if (principal instanceof OAuth2User oauthUser) {
            String oauthId = String.valueOf(oauthUser.getAttributes().get("id"));
            me = userRepository.findByOauthId(oauthId).orElseThrow();
            model.addAttribute("nickname", me.getNickname());
        }

        // 2. 일반 로그인 유저 처리 (CustomUserDetails)
        else if (principal instanceof CustomUserDetails userDetails) {
            me = userDetails.getUser();
            model.addAttribute("nickname", me.getNickname());
        }

        if (me == null) {
            model.addAttribute("error", "로그인 상태를 확인할 수 없습니다.");
            return "redirect:/login";
        }

        try {
            coupleService.createCouple(me, partnerInviteCode);
            model.addAttribute("message", "커플 연결 성공! ❤️");
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("alert", e.getMessage());
            model.addAttribute("inviteCode", me.getInviteCode()); // 실패 시도 다시 띄워줘야 해!
            return "couple/invite";
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
