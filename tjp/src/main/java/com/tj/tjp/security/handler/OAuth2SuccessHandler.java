package com.tj.tjp.security.handler;

import com.tj.tjp.config.FrontendProperties;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.principal.BlockedOAuth2UserPrincipal;
import com.tj.tjp.security.jwt.JwtProvider;
import com.tj.tjp.security.principal.LinkableOAuth2UserPrincipal;
import com.tj.tjp.security.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    private final TokenService tokenService;
    private final FrontendProperties frontendProperties;
    private String frontendRedirectUrl;

    @PostConstruct
    public void init() {
        this.frontendRedirectUrl = frontendProperties.getRedirectUrls().get("oauth2");
    }

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
        // 정상 소셜 로그인 사용자
        issueTokens(response, user);

        // 프론트로 토큰 전달 (리디렉션 or JSON 응답) 리디렉션: 프론트는 쿠키에서 AccessToken꺼내서 처리
        response.sendRedirect(frontendRedirectUrl);
    }

    private void issueTokens(HttpServletResponse response, AuthenticatedUser user) {
        String email = user.getEmail();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // 헤더에 Access Token
        tokenService.issueAccessTokenHeader(response, email, roles);
        // 쿠키에 Refresh Token
        tokenService.issueRefreshTokenCookie(response, email);
    }
}
