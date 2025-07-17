package com.tj.tjp.security.handler;

import com.tj.tjp.config.FrontendProperties;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.principal.OAuth2UserPrincipal;
import com.tj.tjp.security.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final FrontendProperties frontendProperties;
    private String frontendRedirectUrl;

//    @PostConstruct
//    public void init() {
//        // application.yml에 설정된 프론트엔드 콜백 URL 패턴 가져오기
//        this.frontendRedirectUrl = frontendProperties.getRedirectUrls().get("oauth2-login");
//    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 1. OAuth2UserPrincipal 가져오기, 인증된 사용자 정보 추출
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();

        // OAuth2 state 파라미터에서 모드 확인
        String state = request.getParameter("state");
        boolean isLink = false;

        if (state != null && state.startsWith("mode=link:")) {
            isLink = true;
            // 실제 원본 state 필요하다면 뒤에 : 이후 값 추출 가능
        }

//        if (isLink) {
//            log.info("[OAuth2 연동] 소셜 계정 연동 성공 (mode=link)");
//            // 연동 처리
//            // 예시: 프론트 전용 콜백 URL로 리디렉트
//            response.sendRedirect("http://localhost:5173/oauth2/link-callback?success=true");
//            // 또는 프론트에서 원하는 연동 완료 페이지 등
//        } else {
//            log.info("[OAuth2 로그인] 일반 로그인/회원가입 플로우");
//            // 기존 회원가입/로그인 처리
//            response.sendRedirect("http://localhost:5173/oauth2/callback?success=true");
//        }

//        boolean isLinkMode = state != null && state.contains(":link");
//
        if (isLink) {
            // 마이페이지에서 소셜 연동 → 연동 완료 페이지로 리다이렉트
            String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-link-complete")
                    .replace("{provider}", provider);
            response.sendRedirect(redirectUrl);
            return;
        }

        // 2. JWT 발급
        issueTokens(response, principal);

        // 3. 대상 프로바이더에 따라 프론트 콜백 URL에 삽입 후 리다이렉트
        String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-login");
        response.sendRedirect(redirectUrl);


//        // 3. registrationId(google, kakao 등) 꺼내기
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
//
//        // 4. 프론트엔드로 리다이렉트
//        String redirect = frontendRedirectUrl.replace("{registrationId}", registrationId);
//        response.sendRedirect(redirect);

    }

    private void issueTokens(HttpServletResponse response, AuthenticatedUser user) {
        String email = user.getEmail();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // 1) JWT 액세스 토큰을 응답 헤더에 세팅
        tokenService.issueAccessTokenHeader(response, email, roles);
        // 2) JWT 리프레시 토큰을 HttpOnly 쿠키로 세팅
        tokenService.issueRefreshTokenCookie(response, email);
    }
}
