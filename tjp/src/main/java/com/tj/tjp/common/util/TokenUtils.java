package com.tj.tjp.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenUtils {
    private static final String ACCESS_HEADER    = "Access-Token";
    private static final String REFRESH_HEADER   = "Refresh-Token";
    private static final String ACCESS_COOKIE    = "accessToken";
    private static final String REFRESH_COOKIE   = "refreshToken";

    /** 헤더 전용으로 Access-Token 읽기 */
    public static String getAccessTokenFromHeader(HttpServletRequest req) {
        String header = req.getHeader(ACCESS_HEADER);
        if (header == null || header.isBlank()) return null;
        return header.startsWith("Bearer ")
                ? header.substring(7)
                : header;
    }

    /** 헤더 전용으로 Refresh-Token 읽기 */
    public static String getRefreshTokenFromHeader(HttpServletRequest req) {
        String header = req.getHeader(REFRESH_HEADER);
        if (header == null || header.isBlank()) return null;
        return header.startsWith("Bearer ")
                ? header.substring(7)
                : header;
    }

    /** 쿠키 전용으로 Access-Token 읽기 */
    public static String getAccessTokenFromCookie(HttpServletRequest req) {
        return CookieUtils.getCookieValue(req, ACCESS_COOKIE);
    }

    /** 쿠키 전용으로 Refresh-Token 읽기 */
    public static String getRefreshTokenFromCookie(HttpServletRequest req) {
        return CookieUtils.getCookieValue(req, REFRESH_COOKIE);
    }

    /**
     * 하이브리드: 헤더 우선 → 없으면 쿠키
     */
    public static String getAccessToken(HttpServletRequest req) {
        String token = getAccessTokenFromHeader(req);
        return (token != null) ? token : getAccessTokenFromCookie(req);
    }

//    public static String getRefreshToken(HttpServletRequest req) {
//        String token = getRefreshTokenFromHeader(req);
//        return (token != null) ? token : getRefreshTokenFromCookie(req);
//    }
    public static String getRefreshToken(HttpServletRequest req) {
        String token = getRefreshTokenFromHeader(req);
        log.info("🔍 Header에서 가져온 Refresh Token: " + token);

        if (token == null || token.isBlank()) {
            token = getRefreshTokenFromCookie(req);
            log.info("🔍 Cookie에서 가져온 Refresh Token: " + token);
        }

        return token;
    }
}
