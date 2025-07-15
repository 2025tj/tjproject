package com.tj.tjp.security.principal;

import com.tj.tjp.entity.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class BlockedOAuth2UserPrincipal implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final String reason;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

//    public String getProvider() {
//        return user.getProvider().name(); // or .toString()가능? "GOOGLE" 등
//    }

}
