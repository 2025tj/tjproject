package com.tj.tjp.domain.auth.security.principal;

import com.tj.tjp.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class OAuth2UserPrincipal implements OAuth2User, AuthenticatedUser {
    private final User user;
    private final Map<String, Object> attibutes;
    private final List<GrantedAuthority> authorities;

    public OAuth2UserPrincipal(User user, Map<String, Object> attibutes) {
        this.user = user;
        this.attibutes = attibutes;
        this.authorities=user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return user.getEmail(); // 또는 provider에서 주는 id
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attibutes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }
}
