package com.project.withus.controller;

import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public String signup(@RequestParam String loginId,
            @RequestParam String nickname,
            @RequestParam String password) {

        if (userRepository.findByLoginId(loginId).isPresent()) {
            return "redirect:/signup?error=loginId";
        }

        User user = User.builder()
                .loginId(loginId)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        return "redirect:/login?signupSuccess";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false) String error, Model model) {
        if ("loginId".equals(error)) {
            model.addAttribute("errorMessage", "이미 사용 중인 아이디입니다.");
        }
        return "signup";
    }

    @PostMapping("/nickname/edit")
    public String updateNickname(@RequestParam String nickname, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("LOGIN_USER");
        if (user == null) {
            return "redirect:/login?error=session";
        }

        user.setNickname(nickname);
        userRepository.save(user);

        String encodedMsg = URLEncoder.encode("닉네임이 변경되었습니다", StandardCharsets.UTF_8);
        return "redirect:/?message=" + encodedMsg;
    }
}
