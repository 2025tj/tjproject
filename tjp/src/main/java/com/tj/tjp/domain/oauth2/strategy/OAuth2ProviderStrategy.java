package com.tj.tjp.domain.oauth2.strategy;

import java.util.Map;

public interface OAuth2ProviderStrategy {
    String extractProviderId(Map<String, Object> attributes);
    String extractEmail(Map<String, Object> attributes);
}
