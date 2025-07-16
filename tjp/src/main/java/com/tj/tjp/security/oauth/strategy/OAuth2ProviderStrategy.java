package com.tj.tjp.security.oauth.strategy;

import java.util.Map;

public interface OAuth2ProviderStrategy {
    String extractProviderId(Map<String, Object> attributes);
}
