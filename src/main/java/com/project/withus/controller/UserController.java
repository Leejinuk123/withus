package com.project.withus.controller;

import com.project.withus.domain.user.CustomUserDetails;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

        // loginId 중복 체크
        if (userRepository.findByLoginId(loginId).isPresent()) {
            return "redirect:/signup?error=loginId";
        }

        // 사용자 저장
        User user = User.builder()
                .loginId(loginId)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);

        return "redirect:/login?signupSuccess";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false) String error,
            Model model) {
        if ("loginId".equals(error)) {
            model.addAttribute("errorMessage", "이미 사용 중인 아이디입니다.");
        }
        return "signup";
    }

    @PostMapping("/nickname/edit")
    public String updateNickname(@AuthenticationPrincipal Object principal,
            @RequestParam String nickname) {
        User user = extractUser(principal);
        user.setNickname(nickname);
        userRepository.save(user);

        String encodedMsg = URLEncoder.encode("닉네임이 변경되었습니다", StandardCharsets.UTF_8);
        return "redirect:/?message=" + encodedMsg;
    }

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
