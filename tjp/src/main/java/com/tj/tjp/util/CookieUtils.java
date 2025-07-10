package com.tj.tjp.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtils {

    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static String getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, "accessToken");
    }

    public static String getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, "refreshToken");
    }
}
