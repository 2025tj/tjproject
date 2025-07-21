package com.tj.tjp.domain.oauth2.handler;

import com.tj.tjp.domain.oauth2.dto.StateInfo;
import com.tj.tjp.common.util.CryptoUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CryptoUtils cryptoUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        String provider = token.getAuthorizedClientRegistrationId();

        // State 파라미터 복호화
        String encryptedState = request.getParameter("state");
        StateInfo stateInfo = decryptState(encryptedState);

        if (stateInfo == null || !stateInfo.isValid()) {
            log.warn("유효하지 않은 OAuth2 state: {}", encryptedState);
            response.sendRedirect("/login?error=invalid_state");
            return;
        }

        String mode = stateInfo.getMode();
        String oneTimeToken = stateInfo.getToken();

        log.info("OAuth2 인증 성공: provider={}, mode={}, token={}",
                provider, mode, oneTimeToken != null ? "***" : null);

        try {
            if ("signup".equals(mode)) {
                handleSignupMode(response, oAuth2User, provider, oneTimeToken);
            } else if ("link".equals(mode)) {
                handleLinkMode(response, oAuth2User, provider, oneTimeToken);
            } else {
                handleDefaultMode(response, oAuth2User, provider);
            }
        } catch (Exception e) {
            log.error("OAuth2 인증 처리 실패", e);
            response.sendRedirect("/login?error=oauth2_processing_failed");
        }
    }

    private StateInfo decryptState(String encryptedState) {
        try {
            if (encryptedState == null) return null;

            String decryptedJson = cryptoUtils.decrypt(encryptedState);
            return StateInfo.fromJson(decryptedJson);

        } catch (Exception e) {
            log.error("OAuth2 State 복호화 실패", e);
            return null;
        }
    }

    private void handleSignupMode(HttpServletResponse response, OAuth2User oAuth2User,
                                  String provider, String oneTimeToken) throws IOException {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 회원가입 페이지로 리다이렉트 (암호화된 정보 포함)
        String redirectUrl = String.format("/signup/oauth2?provider=%s&email=%s&name=%s&token=%s",
                provider,
                URLEncoder.encode(email, StandardCharsets.UTF_8),
                URLEncoder.encode(name, StandardCharsets.UTF_8),
                oneTimeToken != null ? oneTimeToken : "");

        response.sendRedirect(redirectUrl);
    }

    private void handleLinkMode(HttpServletResponse response, OAuth2User oAuth2User,
                                String provider, String oneTimeToken) throws IOException {
        // 계정 연동 처리
        if (oneTimeToken != null) {
            // 일회용 토큰으로 사용자 식별 후 연동 처리
            String redirectUrl = String.format("/account/link/complete?provider=%s&token=%s",
                    provider, oneTimeToken);
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/account/link?error=missing_token");
        }
    }

    private void handleDefaultMode(HttpServletResponse response, OAuth2User oAuth2User,
                                   String provider) throws IOException {
        // 기본 로그인 처리
        response.sendRedirect("/dashboard");
    }
}