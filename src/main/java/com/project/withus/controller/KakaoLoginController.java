package com.project.withus.controller;

import com.project.withus.domain.user.User;
import com.project.withus.dto.KakaoUserDto;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.KakaoApiService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoApiService kakaoApiService;
    private final UserRepository userRepository;

    @PostMapping("/login/kakao/token")
    @ResponseBody
    public String kakaoLogin(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpServletRequest
    ) {
        String token = request.get("token");

        KakaoUserDto kakaoUser = kakaoApiService.getUserInfo(token);

        // 기존 사용자 찾거나 없으면 생성
        User user = userRepository.findByOauthId(kakaoUser.oauthId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .oauthId(kakaoUser.oauthId())
                            .nickname(kakaoUser.nickname())
                            .loginId(null)
                            .password("")
                            .build();
                    return userRepository.save(newUser);
                });

        // 세션에 저장해서 로그인 처리
        httpServletRequest.getSession().setAttribute("LOGIN_USER", user);

        return "ok";
    }
}