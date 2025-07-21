package com.tj.tjp.domain.auth.security.principal;

import com.tj.tjp.domain.user.entity.User;

import java.util.Map;

public class LinkableOAuth2UserPrincipal extends OAuth2UserPrincipal {
    public LinkableOAuth2UserPrincipal(User user, Map<String, Object> attributes) {
        super(user, attributes);
    }
    public boolean isLinkable() {
        return true;
    }
//    public ProviderType getProvider() {
//        return getUser().getProvider();
//    }
}
