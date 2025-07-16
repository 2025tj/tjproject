package com.tj.tjp.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OneTimeLinkCookieProvider {

    private final OneTimeTokenProvider tokenProvider;

    @Value("${app.one-time-jwt.expiry-minutes}")
    private long expiryMinutes;

    public OneTimeLinkCookieProvider(OneTimeTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * 이메일·provider로 생성한 one-time JWT를 HttpOnly 쿠키로 감싸 전달합니다.
     */
    public ResponseCookie createOneTimeLinkCookie(String email, String provider) {
        String token = tokenProvider.createToken(email, provider);
        return ResponseCookie.from("oneTimeLink", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/social")  // pending 계열 엔드포인트에 맞춰 조정
                .maxAge(Duration.ofMinutes(expiryMinutes))
                .sameSite("Lax")
                .build();
    }
}
