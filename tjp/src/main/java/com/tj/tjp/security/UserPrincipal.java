package com.tj.tjp.security;

import com.tj.tjp.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements UserDetails, OAuth2User {
    private final String email;
//    private final Collection<? extends GrantedAuthority> authorities;
    private final String password;
    private final List<String> roles;
    private Map<String, Object> attributes;

    public UserPrincipal(String email, String password, List<String> roles) {
        this.email =email;
        this.password =password;
        this.roles=roles;
    }

    // 일반로그인
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream().toList()
        );
    }
    // Oauth2 전용
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal principal = create(user);
        principal.setAttributes(attributes);
        return principal;
    }

    public List<String> getRoleList() {
        return roles;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes =attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
