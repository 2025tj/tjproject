package com.tj.tjp.security.oauth.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("google")
public class GoogleOAuth2ProviderStrategy implements OAuth2ProviderStrategy{
    @Override
    public String extractProviderId(Map<String, Object> attributes) {
        return (String) attributes.get("sub");
    }
}
