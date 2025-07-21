package com.tj.tjp.common.exception;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2ProviderNotLinkedException extends OAuth2AuthenticationException {
    public OAuth2ProviderNotLinkedException(String provider) {
        super(new OAuth2Error("PROVIDER_NOT_LINKED", "지원하지 않는 프로바이더: " + provider, null));
    }
}
