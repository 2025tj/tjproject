package com.tj.tjp.common.exception;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Getter
public class OAuth2LinkRequiredException extends OAuth2AuthenticationException {

    private final String email;
    private final String provider;

    public OAuth2LinkRequiredException(String email, String provider) {
        super(new OAuth2Error("LINK_REQUIRED", "연동을 위해 로컬 로그인 필요: " + email + " ("+provider+")", null));
        this.email = email;
        this.provider =provider;
    }
}