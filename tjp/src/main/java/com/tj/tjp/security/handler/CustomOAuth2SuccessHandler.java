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
//        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();


//        // 2) 신규 사용자인가?
//        if (principal.getUser().isNewUser()) {
//            // 가입 폼으로: /oauth2/signup/{registrationId}?email=…
//            String signupTemplate =
//                    frontendProperties.getRedirectUrls().get("oauth2-signup");
//            String signupUrl = UriComponentsBuilder
//                    .fromUriString(signupTemplate)
//                    .queryParam("email", principal.getEmail())
//                    .buildAndExpand(registrationId)
//                    .toUriString();
//            response.sendRedirect(signupUrl);
//            return;
//        }


        // 2. JWT 발급
        issueTokens(response, principal);

        // 3. 대상 프로바이더에 따라 프론트 콜백 URL에 삽입 후 리다이렉트
//        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
//        response.sendRedirect(frontendRedirectUrl.replace("{registrationId}", registrationId));
        String template = frontendProperties.getRedirectUrls().get("oauth2-login"); // e.g. http://localhost:5173/oauth2/link/{registrationId}
//        String redirectUrl = template.replace("{registrationId}", registrationId);

//        response.sendRedirect(redirectUrl);
        response.sendRedirect(template);


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
