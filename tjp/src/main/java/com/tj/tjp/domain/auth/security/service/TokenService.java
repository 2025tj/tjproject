package com.tj.tjp.domain.auth.security.service;

import com.tj.tjp.domain.auth.blacklist.service.RefreshTokenStoreService;
import com.tj.tjp.domain.auth.blacklist.service.TokenBlacklistService;
import com.tj.tjp.infrastructure.config.properties.CookieProperties;
import com.tj.tjp.infrastructure.config.properties.FrontendProperties;
import com.tj.tjp.domain.auth.security.jwt.JwtProvider;
import com.tj.tjp.common.util.TokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final FrontendProperties frontendProperties;
    private final CookieProperties cookieProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenStoreService refreshTokenStoreService;

    // -- 내부 유틸 --
    private void setRefreshCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .sameSite(cookieProperties.getSameSite())
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .sameSite(cookieProperties.getSameSite())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    // -- 기존 RT 무효화 --
    public void invalidOldRefreshToken(HttpServletRequest request) {
        String oldToken = extractRefreshTokenFromCookie(request);
        if (oldToken != null && jwtProvider.validateRefreshToken(oldToken)) {
            try {
                String email = jwtProvider.getEmailFromToken(oldToken);
                long ttl = jwtProvider.getRefreshTokenRemainingMillis(oldToken);
                if (ttl > 0) {
                    tokenBlacklistService.blacklistRefreshToken(oldToken, Math.max(ttl, 1000));
                    log.info("Old refresh token blacklisted: {}", oldToken);
                }
                // 사용자별 저장소에서도 제거
                refreshTokenStoreService.delete(email);
        } catch (Exception e) {
                log.warn("invalidOldRefreshToken: parse failed: {}", e.getMessage());
            }
            }
    }

    /** Access 토큰만 헤더로 발급 */
    public void issueAccessTokenHeader(HttpServletResponse response, String email, List<String> roles) {
        String accessToken = jwtProvider.createAccessToken(email, roles);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Access-Token");
        response.setHeader("Access-Token", "Bearer " + accessToken);
    }

    /** Refresh 토큰만 HttpOnly 쿠키로 발급 */
    public void issueRefreshTokenCookie(HttpServletResponse response, String email) {
        String refreshToken = jwtProvider.createRefreshToken(email);
        long ttl = jwtProvider.getRefreshTokenRemainingMillis(refreshToken);
        // 저장(해시) + 쿠키 세팅
        refreshTokenStoreService.save(email, refreshToken, Math.max(ttl, 1000));
        setRefreshCookie(response, refreshToken, ttl / 1000);
    }
//    public void issueRefreshTokenCookie(HttpServletResponse response, String email) {
//        String refreshToken = jwtProvider.createRefreshToken(email);
//
//        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
//                .httpOnly(true)
//                .secure(cookieProperties.isSecure())
//                .path("/")
//                .sameSite(cookieProperties.getSameSite())
//                .maxAge(7 * 24 * 60 * 60)
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//    }
//    public void issueRefreshTokenCookie(HttpServletResponse response, String email) {
//        String refreshToken = jwtProvider.createRefreshToken(email);
//        response.addHeader(HttpHeaders.SET_COOKIE,
//                String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None; Secure",
//                        refreshToken, 7*24*60*60)
//        );
//    }


    /**
     * 리프레시 토큰 검증 후 새 Access Token 헤더로 발급
     */
//    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
//        String refreshToken = TokenUtils.getRefreshToken(request);
//        log.info("Received refresh token: {}", refreshToken);
//        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
//            log.warn("No refresh token found in request");
//            throw new RuntimeException("Invalid or missing refresh token");
//        }
//
//        boolean valid = jwtProvider.validateRefreshToken(refreshToken);
//        log.info("Refresh token validation result: {}", valid);
//
//        if (!valid) {
//            log.warn("Refresh token is invalid");
//            throw new RuntimeException("Invalid or missing refresh token");
//        }
//
//        String email = jwtProvider.getEmailFromToken(refreshToken);
//        log.info("Email extracted from refresh token: {}", email);
//        List<String> roles = jwtProvider.getRolesFromToken(refreshToken);
//        issueAccessTokenHeader(response, email, roles);
//    }
//    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
//        String oldRefreshToken = TokenUtils.getRefreshToken(request);
//        log.info("Received refresh token: {}", oldRefreshToken);
//        if (oldRefreshToken == null || !jwtProvider.validateRefreshToken(oldRefreshToken)) {
//            log.warn("No refresh token found in request");
//            throw new RuntimeException("Invalid or missing refresh token");
//        }
//
//        long ttl = jwtProvider.getRefreshTokenRemainingMillis(oldRefreshToken);
//        if (ttl > 0) {
//            tokenBlacklistService.blacklistRefreshToken(oldRefreshToken, Math.max(ttl, 1000));
//            log.info("Old refresh token blacklisted");
//        }
//
//        String email = jwtProvider.getEmailFromToken(oldRefreshToken);
//        log.info("Email extracted from refresh token: {}", email);
//        List<String> roles = jwtProvider.getRolesFromToken(oldRefreshToken);
//
//        // 새 Access Token 헤더로 발급
//        issueAccessTokenHeader(response, email, roles);
//
//        // 새 Refresh Token 쿠키 발급
//        issueRefreshTokenCookie(response, email);
//
//        log.info("New tokens issued");
//    }
    // --- rt 회전 ---
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = TokenUtils.getRefreshToken(request);
        log.info("Received refresh token: {}", oldRefreshToken);
        if (oldRefreshToken == null || !jwtProvider.validateRefreshToken(oldRefreshToken)) {
            log.warn("No valid refresh token found in request");
            throw new RuntimeException("Invalid or missing refresh token");
        }

        //블랙 리스트 차단
        if (tokenBlacklistService.isBlacklisted(oldRefreshToken)) {
            clearRefreshCookie(response);
            log.warn("Refresh token is blacklisted (reuse or logged-out). Forcing re-auth.");
            throw new RuntimeException("Re-authentication required");
        }

        String email = jwtProvider.getEmailFromToken(oldRefreshToken);

        // 저장된 해시와 불일치 => 재사용/탈취 의심
        if (!refreshTokenStoreService.matches(email, oldRefreshToken)) {
            long ttl = jwtProvider.getRefreshTokenRemainingMillis(oldRefreshToken);
            if (ttl > 0) {
                tokenBlacklistService.blacklistRefreshToken(oldRefreshToken, Math.max(ttl, 1000));
            }
            refreshTokenStoreService.delete(email);
            clearRefreshCookie(response); // 강제 로그아웃
            log.warn("RT reuse detected for {}. Force logout.", email);
            throw new RuntimeException("Re-authentication required");
        }

        // 정상: 회전
        long ttl = jwtProvider.getRefreshTokenRemainingMillis(oldRefreshToken);
        if (ttl > 0) {
            tokenBlacklistService.blacklistRefreshToken(oldRefreshToken, Math.max(ttl, 1000));
            log.info("Old refresh token blacklisted");
        }

        List<String> roles = jwtProvider.getRolesFromToken(oldRefreshToken);

        // 새 AT/RT 발급
        issueAccessTokenHeader(response, email, roles);
        issueRefreshTokenCookie(response, email); // 저장 + 쿠키 동시 처리
        log.info("New tokens issued (rotated) for {}", email);
    }

    // 리프레시 토큰 삭제 (DB + 쿠키) - 로그아웃
    public void deleteRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                log.info("Cookie: {} = {}", c.getName(), c.getValue());
            }
        } else {
            log.warn("No cookies found in request");
        }

        String refreshToken = extractRefreshTokenFromCookie(request);
        log.info("Extracted refresh token from cookie: {}", refreshToken);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
            try {
                String email = jwtProvider.getEmailFromToken(refreshToken);
                long ttl = jwtProvider.getRefreshTokenRemainingMillis(refreshToken);
                if (ttl > 0) {
                    long safeTtl = Math.max(ttl, 1000);
                    tokenBlacklistService.blacklistRefreshToken(refreshToken, safeTtl);
                }
                // 사용자 저장소에서 삭제
                refreshTokenStoreService.delete(email);
            } catch (Exception e) {
                log.warn("logout: cannot parse RT: {}", e.getMessage());
            }
        }
        clearRefreshCookie(response);
    }
//    public void deleteRefreshToken(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie c : cookies) {
//                log.info("Cookie: {} = {}", c.getName(), c.getValue());
//            }
//        } else {
//            log.warn("No cookies found in request");
//        }
//        // 1. DB에서 삭제(redis 구현 필요)
////        refreshTokenRepository.deleteByUserId(userId);
//        String refreshToken = extractRefreshTokenFromCookie(request);
//        log.info("Extracted refresh token from cookie: {}", refreshToken);
//        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
//            long ttl = jwtProvider.getRefreshTokenRemainingMillis(refreshToken);
//            if (ttl > 0) {
//                long safeTtl = Math.max(ttl, 1000);
//                tokenBlacklistService.blacklistRefreshToken(refreshToken, safeTtl);
//            }
//        }
//
//        // 2. 쿠키 삭제
//        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
//                .httpOnly(true)
//                .secure(cookieProperties.isSecure())
//                .path("/")
//                .sameSite(cookieProperties.getSameSite())
//                .maxAge(0)
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
//    }

    /**
     * Access Token 검증 (서명·만료 검사)
     * @param token 순수 토큰 문자열
     * @return 유효하면 true
     */
    public boolean validateAccessToken(String token) {
        return jwtProvider.validateToken(token);
    }

    /**
     * Access Token에서 이메일 추출
     */
    public String getEmailFromAccessToken(String token) {
        return jwtProvider.getEmailFromToken(token);
    }
}
