package com.tj.tjp.common.exception;


import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Getter
public class OAuth2SignupRequiredException extends OAuth2AuthenticationException {
    private final String email, provider, providerId;
    public OAuth2SignupRequiredException(String email, String provider, String providerId) {
        super(new OAuth2Error("SIGNUP_REQUIRED"), "Signup required");
        this.email = email; this.provider = provider; this.providerId= providerId;
    }
}
