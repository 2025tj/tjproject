package com.tj.tjp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        log.info("[OAuth2SuccessHandler] 소셜 로그인 성공");

        try {
            Object principal = authentication.getPrincipal();
            log.info("Principal class: {}", principal.getClass().getName());
        } catch (Exception e) {
            log.error("Authentication principal error", e);
        }

        UserPrincipal userPrincipal =(UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getUsername();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();

        log.info("[OAuth2SuccessHandler] 사용자 email: {}", email);
        log.info("[OAuth2SuccessHandler] 사용자 roles: {}", roles);

        // access & refresh token 생성
        String accessToken = jwtProvider.createAccessToken(email, roles);
        String refreshToken = jwtProvider.createRefreshToken(email);

        log.info("[OAuth2SuccessHandler] accessToken 생성 완료");
        log.info("[OAuth2SuccessHandler] refreshToken 생성 완료");


        // accessToken은 localStorage에 저장할 것이므로 클라이언트에 보내줌
        // redirect URL에 쿼리로 포함해도 되고 JSON으로도 가능

        // refreshToken을 쿠키로 저장
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // js 접근 불가
        cookie.setSecure(false); // https에서만?( 개발중엔 false)
        cookie.setPath("/");
        cookie.setMaxAge(7*24*60*60); // 7일 유지
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);

        // 프론트로 토큰 전달 (리디렉션 or JSON 응답)
//        response.sendRedirect("/login/success?token="+token);
        // 프론트엔드로 accessToken을 쿼리로 넘김
        String redirectUrl ="http://localhost:5173/oauth2/redirect?accessToken="+accessToken;
//        response.sendRedirect("http://localhost:5173");
        log.info("[OAuth2SuccessHandler] 프론트로 리디렉션: {}", redirectUrl);
        response.sendRedirect(redirectUrl);

    }


}
