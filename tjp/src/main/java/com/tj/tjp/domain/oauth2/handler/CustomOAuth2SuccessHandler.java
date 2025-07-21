package com.tj.tjp.domain.oauth2.handler;

import com.tj.tjp.infrastructure.config.properties.FrontendProperties;
import com.tj.tjp.domain.oauth2.dto.StateInfo;
import com.tj.tjp.domain.auth.security.principal.AuthenticatedUser;
import com.tj.tjp.domain.auth.security.principal.OAuth2UserPrincipal;
import com.tj.tjp.domain.auth.security.service.TokenService;
import com.tj.tjp.common.util.OAuth2StateEncoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final FrontendProperties frontendProperties;
//    private final CryptoUtils cryptoUtils;
    private final OAuth2StateEncoder stateEncoder;
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
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // OAuth2 state 파라미터에서 모드 확인(및 복호화, 암호화 지원)
        String state = request.getParameter("state");
        StateInfo stateInfo = parseState(state);

        log.info("OAuth2 인증 성공: provider={}, mode={}, token={}",
                provider, stateInfo.getMode(), stateInfo.getToken() != null ? "***" : null);

        try {
            String mode = stateInfo.getMode();

            if ("signup".equals(mode)) {
                handleSignupMode(response, principal, provider, stateInfo);
            } else if ("link".equals(mode)) {
                handleLinkMode(response, principal, provider, stateInfo);
            } else {
                handleDefaultMode(response, principal, provider);
            }
        } catch (Exception e) {
            log.error("OAuth2 인증 처리 실패", e);
            // 기존 설정 활용: error URL 사용 (없으면 기본 에러 페이지)
            String errorUrl = frontendProperties.getRedirectUrls().getOrDefault("error", "/error");
            response.sendRedirect(errorUrl + "?error=oauth2_processing_failed");
        }
    }

    /**
     * State 파라미터 파싱 (암호화 지원)
     */
    private StateInfo parseState(String state) {
        if (state == null) {
            return StateInfo.builder().build();
        }

        try {
            // 1. 암호화된 State 복호화 시도
//            String decryptedJson = cryptoUtils.decrypt(state);
            String decryptedJson = stateEncoder.decrypt(state);
            StateInfo stateInfo = StateInfo.fromJson(decryptedJson);

            if (stateInfo.isValid()) {
                log.debug("암호화된 State 복호화 성공");
                return stateInfo;
            }
        } catch (Exception e) {
            log.debug("암호화된 State 복호화 실패, 기존 방식으로 파싱 시도");
        }

        // 2. 기존 방식 파싱 (하위 호환)
        return parseLegacyState(state);
    }

    /**
     * 기존 방식 State 파싱 (하위 호환)
     */
    private StateInfo parseLegacyState(String state) {
        StateInfo.StateInfoBuilder builder = StateInfo.builder();

        if (state.startsWith("mode=link:")) {
            builder.mode("link");
            builder.originalState(state.substring("mode=link:".length()));
        } else if (state.startsWith("mode=signup:")) {
            builder.mode("signup");
            builder.originalState(state.substring("mode=signup:".length()));
        } else if (state.contains("mode=")) {
            // "mode=link&token=abc123:original_state" 형태 파싱
            String[] parts = state.split(":", 2);
            if (parts.length == 2) {
                String params = parts[0];
                String originalState = parts[1];

                if (params.contains("mode=link")) {
                    builder.mode("link");
                } else if (params.contains("mode=signup")) {
                    builder.mode("signup");
                }

                // token 파라미터 추출
                if (params.contains("token=")) {
                    String tokenPart = params.split("token=")[1];
                    if (tokenPart.contains("&")) {
                        tokenPart = tokenPart.split("&")[0];
                    }
                    builder.token(tokenPart);
                }

                builder.originalState(originalState);
            }
        }

        return builder.build();
    }

    /**
     * 회원가입 모드 처리
     */
    private void handleSignupMode(HttpServletResponse response, OAuth2UserPrincipal principal,
                                  String provider, StateInfo stateInfo) throws IOException {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String oneTimeToken = stateInfo.getToken();

        // 기존 설정 활용: oauth2-signup URL 패턴 사용
        String redirectUrlPattern = frontendProperties.getRedirectUrls().get("oauth2-signup");
        String redirectUrl = redirectUrlPattern.replace("{registrationId}", provider);

        // 쿼리 파라미터 추가
        redirectUrl += String.format("?email=%s&name=%s&token=%s",
                URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8),
                URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8),
                oneTimeToken != null ? oneTimeToken : "");

        response.sendRedirect(redirectUrl);
    }

    /**
     * 계정 연동 모드 처리
     */
    private void handleLinkMode(HttpServletResponse response, OAuth2UserPrincipal principal,
                                String provider, StateInfo stateInfo) throws IOException {

        String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-link-complete")
                .replace("{provider}", provider);

        // provider를 쿼리 파라미터로 추가(프론트에서 받음)
        redirectUrl += "?provider=" + provider;

        response.sendRedirect(redirectUrl);
    }
//    private void handleLinkMode(HttpServletResponse response, OAuth2UserPrincipal principal,
//                                String provider, StateInfo stateInfo) throws IOException {
//        String oneTimeToken = stateInfo.getToken();
//
////        // 로그인 유지
////        issueTokens(response, principal);
//
//        if (oneTimeToken != null) {
//            // 기존 설정 활용: oauth2-link-complete URL 사용
//            String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-link-complete");
//            redirectUrl += String.format("?provider=%s&token=%s&success=true", provider, oneTimeToken);
//            response.sendRedirect(redirectUrl);
//        } else {
//            // 기존 설정 활용: oauth2-link URL 패턴 사용
//            String redirectUrlPattern = frontendProperties.getRedirectUrls().get("oauth2-link");
//            String redirectUrl = redirectUrlPattern.replace("{registrationId}", provider);
//            redirectUrl += "?error=missing_token";
//            response.sendRedirect(redirectUrl);
//        }
//    }

    /**
     * 기본 로그인 처리
     */
    private void handleDefaultMode(HttpServletResponse response, OAuth2UserPrincipal principal,
                                   String provider) throws IOException {
        // JWT 토큰 발급
        issueTokens(response, principal);

        // 기존 설정 활용: oauth2-login URL 사용
        String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-login");
        response.sendRedirect(redirectUrl);
    }

    /**
     * JWT 토큰 발급
     */
    private void issueTokens(HttpServletResponse response, AuthenticatedUser user) {
        String email = user.getEmail();
        List<String> roles = user.getUser().getRoles().stream().toList();

        // 1) JWT 액세스 토큰을 응답 헤더에 세팅
        tokenService.issueAccessTokenHeader(response, email, roles);
        // 2) JWT 리프레시 토큰을 HttpOnly 쿠키로 세팅
        tokenService.issueRefreshTokenCookie(response, email);
    }
}

//        boolean isLink = false;
//
//        if (state != null && state.startsWith("mode=link:")) {
//            isLink = true;
//            // 실제 원본 state 필요하다면 뒤에 : 이후 값 추출 가능
//        }
//
////        if (isLink) {
////            log.info("[OAuth2 연동] 소셜 계정 연동 성공 (mode=link)");
////            // 연동 처리
////            // 예시: 프론트 전용 콜백 URL로 리디렉트
////            response.sendRedirect("http://localhost:5173/oauth2/link-callback?success=true");
////            // 또는 프론트에서 원하는 연동 완료 페이지 등
////        } else {
////            log.info("[OAuth2 로그인] 일반 로그인/회원가입 플로우");
////            // 기존 회원가입/로그인 처리
////            response.sendRedirect("http://localhost:5173/oauth2/callback?success=true");
////        }
//
////        boolean isLinkMode = state != null && state.contains(":link");
////
//        if (isLink) {
//            // 마이페이지에서 소셜 연동 → 연동 완료 페이지로 리다이렉트
//            String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
//            String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-link-complete")
//                    .replace("{provider}", provider);
//            response.sendRedirect(redirectUrl);
//            return;
//        }
//
//        // 2. JWT 발급
//        issueTokens(response, principal);
//
//        // 3. 대상 프로바이더에 따라 프론트 콜백 URL에 삽입 후 리다이렉트
//        String redirectUrl = frontendProperties.getRedirectUrls().get("oauth2-login");
//        response.sendRedirect(redirectUrl);
//
//
////        // 3. registrationId(google, kakao 등) 꺼내기
////        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
////        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
////
////        // 4. 프론트엔드로 리다이렉트
////        String redirect = frontendRedirectUrl.replace("{registrationId}", registrationId);
////        response.sendRedirect(redirect);
//
//    }
//
//    private void issueTokens(HttpServletResponse response, AuthenticatedUser user) {
//        String email = user.getEmail();
//        List<String> roles = user.getUser().getRoles().stream().toList();
//
//        // 1) JWT 액세스 토큰을 응답 헤더에 세팅
//        tokenService.issueAccessTokenHeader(response, email, roles);
//        // 2) JWT 리프레시 토큰을 HttpOnly 쿠키로 세팅
//        tokenService.issueRefreshTokenCookie(response, email);
//    }
//}
