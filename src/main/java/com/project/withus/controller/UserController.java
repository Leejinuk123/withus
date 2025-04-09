package com.project.withus.controller;

import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public String signup(@RequestParam String nickname,
            @RequestParam String password) {

        // 닉네임 중복 체크
        if (userRepository.findByNickname(nickname).isPresent()) {
            // 같은 닉네임이 존재 → 회원가입 페이지로 리다이렉트 (쿼리 파라미터로 알림 가능)
            return "redirect:/signup?error=nickname";
        }

        // 사용자 저장
        User user = User.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .oauth(false)
                .build();

        userRepository.save(user);

        return "redirect:/login?signupSuccess";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false) String error,
            Model model) {
        if ("nickname".equals(error)) {
            model.addAttribute("errorMessage", "이미 사용 중인 닉네임입니다.");
        }
        return "signup";
    }

}
