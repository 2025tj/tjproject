package com.tj.tjp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        OAuth2User oAuth2User =(OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String token = jwtProvider.createToken(email);

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", token);
        cookie.setHttpOnly(true); // js 접근 불가
//        cookie.setSecure(true); // https에서만?( 개발중엔 false)
        cookie.setPath("/");
        cookie.setMaxAge(60*60); // 1시간 유지

        response.addCookie(cookie);

        // 프론트로 토큰 전달 (리디렉션 or JSON 응답)
//        response.sendRedirect("/login/success?token="+token);
//        response.sendRedirect("http://localhost:5173/oauth2/redirect?token="+token);
        response.sendRedirect("http://localhost:5173");


    }


}
