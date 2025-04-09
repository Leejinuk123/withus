package com.project.withus.controller;

import com.project.withus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login/custom")
    public String login(@RequestParam String loginId,
            @RequestParam String password,
            HttpServletRequest request) {

        return userRepository.findByLoginId(loginId)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    request.getSession().setAttribute("LOGIN_USER", user); // ✅ 세션에 유저 저장
                    return "redirect:/";
                })
                .orElse("redirect:/login?error&loginId=" + loginId);
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate(); // ✅ 세션 초기화
        return "redirect:/login";
    }
}
