package com.project.withus.config;

import com.project.withus.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/signup",
                                "/login/custom",
                                "/login/oauth2/code/**",
                                "/oauth/**", // ✅ 닉네임 설정 페이지 접근 허용
                                "/css/**", "/js/**", "/images/**", "/icons/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // ✅ 공통 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // defaultSuccessUrl은 카카오 유저가 최초 로그인 시 실패할 수 있으므로 리다이렉트 처리로 대체됨
                        .defaultSuccessUrl("/", true)
                );

        return http.build();
    }
}
