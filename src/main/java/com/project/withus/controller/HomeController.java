package com.project.withus.controller;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import com.project.withus.service.LetterService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public String home(
            @RequestParam(required = false) Boolean noCouple,
            Model model,
            HttpServletRequest request
    ) {
        User me = (User) request.getSession().getAttribute("LOGIN_USER");

        // ✅ 로그인 안 한 경우 → /login 으로 이동
        if (me == null) {
            return "redirect:/login";
        }

        model.addAttribute("nickname", me.getNickname());
        model.addAttribute("currentURI", request.getRequestURI());

        Optional<Couple> myCouple = coupleService.getMyCouple(me);
        model.addAttribute("notCoupled", myCouple.isEmpty());

        if (myCouple.isPresent()) {
            long dday = coupleService.getDDay(me) + 1;
            model.addAttribute("dday", "D+" + dday);
        }

        long unreadCount = letterService.countUnreadLetters(me);
        model.addAttribute("unreadCount", unreadCount);

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