package com.project.withus.domain.user;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static final String LOGIN_USER = "loginUser";

    public static User getLoginUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER);
    }
}
