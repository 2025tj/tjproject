package com.tj.tjp.security.handler;

import com.tj.tjp.config.properties.FrontendProperties;
import com.tj.tjp.exception.OAuth2SignupRequiredException;
import com.tj.tjp.security.jwt.OneTimeLinkCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final FrontendProperties frontendProps;
    private final OneTimeLinkCookieProvider cookieProvider;

    @Override
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {

//        // 1) LINK_REQUIRED 예외
//        if (ex instanceof OAuth2LinkRequiredException linkEx) {
//            issueOneTimeLink(res, linkEx.getEmail(), linkEx.getProvider());
//            String url = frontendProps.getRedirectUrls().get("oauth2-link")
//                    .replace("{registrationId}", linkEx.getProvider());
//            res.sendRedirect(url);
//            return;
//        }

        // 2) SIGNUP_REQUIRED 예외
        if (ex instanceof OAuth2SignupRequiredException signEx) {
//            issueOneTimeLink(res, signEx.getEmail(), signEx.getProvider(), signEx.getProviderId());
            String url = frontendProps.getRedirectUrls().get("signup");
            res.sendRedirect(url);
            return;
        }

        // 기타 오류 → 로그인 페이지로 리다이렉트
        log.error("OAuth2 인증 실패: {}", ex.getMessage());
        res.sendRedirect("/login?error=oauth2");

//        // 3) 나머지 에러
//        res.sendRedirect(frontendProps.getRedirectUrls().get("login") + "?error");
    }

    private void issueOneTimeLink(HttpServletResponse res, String email, String provider, String providerId) {
        ResponseCookie cookie = cookieProvider.createOneTimeLinkCookie(email, provider, providerId);
        res.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }






//        // 1) 기본 HTTP 상태 401 설정
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//
//        // 2) 예외 타입에 따라 에러 코드 식별
//        String errorCode = "OAUTH2_ERROR";
//        if (exception instanceof OAuth2AuthenticationException oauthEx &&
//                oauthEx.getError() != null) {
//            errorCode = oauthEx.getError().getErrorCode();
//        }
//        String message = exception.getMessage();
//
//        // 3) LINK_REQUIRED인 경우, 프론트의 계정 연동 페이지로 리다이렉트
//        if ("LINK_REQUIRED".equals(errorCode)) {
//            // LINK_REQUIRED 메시지에서 email/provider 파싱
//            String email = extractEmailFromMessage(message);
//            String provider = extractProviderFromMessage(message);
//            try {
//                issueOneTimeLink(response, email, provider);
//            } catch (IOException ioe) {
//                log.error("리다이렉트 처리 중 오류", ioe);
//                // 리다이렉트 실패 시 JSON 폴백
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                objectMapper.writeValue(response.getWriter(), Map.of(
//                        "status",  "REDIRECT_ERROR",
//                        "message", "연동 페이지로 이동 중 오류가 발생했습니다."
//                ));
//            }
//            return;
//        }
//
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        objectMapper.writeValue(response.getWriter(), Map.of(
//                "status", errorCode,
//                "message", message
//        ));
//    }
//
//    // LINK_REQUIRED 메시지에서 email 파싱
//    private String extractEmailFromMessage(String message) {
//        String detail  = message.substring(message.indexOf(':') + 1).trim();
//        return detail.split("\\s+")[0];
//    }
//
//    // LINK_REQUIRED 메시지에서 provider 파싱
//    private String extractProviderFromMessage(String message) {
//        String detail  = message.substring(message.indexOf(':') + 1).trim();
//        String provider = detail.split("\\s+")[1];
//        return provider.replaceAll("[()]", "");
//    }
//
//    private void issueOneTimeLink(HttpServletResponse res, String email, String provider) throws IOException {
//        // 1) 토큰 생성
//        String token = otpProvider.createToken(email, provider);
//        // 2) HttpOnly 쿠키 세팅
//        ResponseCookie cookie = ResponseCookie.from("oneTimeLink", token)
//                .httpOnly(true)
//                .secure(true)
//                .path("/oauth2/link")
//                .maxAge(Duration.ofMinutes(10))
//                .sameSite("Lax")
//                .build();
//        res.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//
//        // 3) 프론트 연동 페이지로 리다이렉트
//        // 예: https://frontend.com/oauth2/link?email={email}&provider={provider}
//        String linkUrl = frontendProperties.getRedirectUrls().get("oauth2-link");
//        res.sendRedirect(linkUrl);
//    }
}
