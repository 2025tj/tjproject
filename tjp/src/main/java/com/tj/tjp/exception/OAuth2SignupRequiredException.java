package com.tj.tjp.exception;


import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Getter
public class OAuth2SignupRequiredException extends OAuth2AuthenticationException {
    private final String email, provider;
    public OAuth2SignupRequiredException(String email, String provider) {
        super(new OAuth2Error("SIGNUP_REQUIRED"), "Signup required");
        this.email = email; this.provider = provider;
    }
}
