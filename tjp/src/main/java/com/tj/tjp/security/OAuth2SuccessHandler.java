package com.tj.tjp.security;

import com.tj.tjp.config.FrontendProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final FrontendProperties frontendProperties;

//    @Value("${frontend.redirect-url}")
//    private String frontendRedirectUrl;
    String frontendRedirectUrl = frontendProperties.getRedirectUrl();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedUser user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid principal");
            return;
        }
        // 차단된 사용자 처리
        if (principal instanceof BlockedOAuth2UserPrincipal blocked) {
            String error = URLEncoder.encode(blocked.getReason(), StandardCharsets.UTF_8);
            response.sendRedirect(frontendRedirectUrl+"?error="+error);
            return;
        }
        // 연동 가능한 사용자 처리
        if (principal instanceof LinkableOAuth2UserPrincipal linkable) {
            response.sendRedirect(frontendRedirectUrl+"?link=true");
            return;
        }
        // 정상 로그인 사용자 처리 (access, refresh토큰 발급)
        String email = user.getEmail();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // access & refresh token 생성
        String accessToken = jwtProvider.createAccessToken(email, roles);
        String refreshToken = jwtProvider.createRefreshToken(email);

        // accessToken은 localStorage에 저장할 것이므로 클라이언트에 보내줌
        // redirect URL에 쿼리로 포함해도 되고 JSON으로도 가능하지만 쿠키에 담아 보내는게 가장 안전
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(false); // js 접근 허용
        accessTokenCookie.setSecure(false); // HTTPS 적용시 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60); // 1분 내 사용 후 js에서 삭제 권장
        accessTokenCookie.setAttribute("SameSite", "Lax");
        response.addCookie(accessTokenCookie);

        // refreshToken을 쿠키로 저장
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // js 접근 불가
        cookie.setSecure(false); // https에서만?( 개발중엔 false)
        cookie.setPath("/");
        cookie.setMaxAge(7*24*60*60); // 7일 유지
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        // 프론트로 토큰 전달 (리디렉션 or JSON 응답) 리디렉션: 프론트는 쿠키에서 AccessToken꺼내서 처리
        response.sendRedirect(frontendRedirectUrl);
    }
}
