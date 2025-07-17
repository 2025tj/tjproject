package com.tj.tjp.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ErrorResponse {
    private String error;
    private String message;
    private String email;
    private String provider;
    private String providerId;
    private LocalDateTime timestamp;

    //OAuth2LinkRequiredException용
    public static OAuth2ErrorResponse linkRequired(String email, String provider) {
        return OAuth2ErrorResponse.builder()
                .error("SOCIAL_LINK_REQUIRED")
                .message("소셜 계정 연동이 필요합니다.")
                .email(email)
                .provider(provider)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // OAuth2SignupRequiredException용
    public static OAuth2ErrorResponse signupRequired(String email, String provider, String providerId) {
        return OAuth2ErrorResponse.builder()
                .error("SIGNUP_REQUIRED")
                .message("회원가입이 필요합니다.")
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
