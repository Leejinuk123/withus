package com.project.withus.controller;

import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    // ✅ 세션에서 로그인 유저 가져오기
    private User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("LOGIN_USER");
    }

    @GetMapping("/invite")
    public String invitePage(HttpServletRequest request, Model model) {
        User me = getLoginUser(request);
        if (me == null) return "redirect:/login?error=session";

        model.addAttribute("inviteCode", me.getInviteCode());
        return "couple/invite";
    }

    @PostMapping("/invite")
    public String invite(
            @RequestParam String partnerInviteCode,
            HttpServletRequest request,
            Model model) {

        User me = getLoginUser(request);
        if (me == null) {
            return "redirect:/login?error=session";
        }

        try {
            coupleService.createCouple(me, partnerInviteCode);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("alert", e.getMessage());
            model.addAttribute("inviteCode", me.getInviteCode());
            return "couple/invite";
        }
    }

    @PostMapping("/dday-setting")
    public String saveDdaySetting(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            HttpServletRequest request,
            Model model) {

        User user = getLoginUser(request);
        if (user == null) return "redirect:/login?error=session";

        coupleService.updateStartDate(user, startDate);
        model.addAttribute("message", "D-day가 설정되었습니다");
        return "redirect:/";
    }
}
