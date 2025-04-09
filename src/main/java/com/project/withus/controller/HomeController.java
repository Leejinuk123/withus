package com.project.withus.controller;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.user.CustomUserDetails;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import com.project.withus.service.LetterService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final LetterService letterService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Object principal,
            @RequestParam(required = false) Boolean noCouple,
            Model model, HttpServletRequest request) {

        User me = null;

        model.addAttribute("currentURI", request.getRequestURI()); // 👈 추가!

        // 1. 카카오 로그인 유저 처리
        if (principal instanceof OAuth2User oauthUser) {
            String myNickname = ((Map<String, Object>) ((Map<String, Object>) oauthUser.getAttributes().get("kakao_account"))
                    .get("profile")).get("nickname").toString();
            me = userRepository.findByNickname(myNickname).orElseThrow();
            model.addAttribute("nickname", myNickname);
        }

        // 2. 일반 로그인 유저 처리 (CustomUserDetails)
        else if (principal instanceof CustomUserDetails userDetails) {
            me = userDetails.getUser();
            model.addAttribute("nickname", me.getNickname());
        }

        // 3. 로그인된 경우에만 커플, 편지 데이터 처리
        if (me != null) {
            Optional<Couple> myCouple = coupleService.getMyCouple(me);
            model.addAttribute("notCoupled", myCouple.isEmpty());

            if (myCouple.isPresent()) {
                long dday = coupleService.getDDay(me) + 1;
                model.addAttribute("dday", "D+" + dday);
            }

            long unreadCount = letterService.countUnreadLetters(me);
            model.addAttribute("unreadCount", unreadCount);
        }

        // 4. 커플 연결 안 된 경우 메시지
        if (noCouple != null && noCouple) {
            model.addAttribute("alert", "아직 커플 연결이 되지 않아 편지를 사용할 수 없어요 😢");
        }

        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
