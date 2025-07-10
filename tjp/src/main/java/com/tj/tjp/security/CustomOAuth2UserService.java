package com.tj.tjp.security;

import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email= (String) oAuth2User.getAttributes().get("email");

        // db에 사용자 없으면 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(()->userRepository.save(
                        com.tj.tjp.entity.User.builder()
                                .email(email)
                                .roles(Set.of("ROLE_USER"))
                                .build()
                ));
        return UserPrincipal.create(email, oAuth2User.getAttributes());
    }

}
