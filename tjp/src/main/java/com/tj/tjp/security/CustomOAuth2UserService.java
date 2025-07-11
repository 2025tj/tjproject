package com.tj.tjp.security;

import com.tj.tjp.entity.ProviderType;
import com.tj.tjp.entity.User;
import com.tj.tjp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(request);

            String providerStr = request.getClientRegistration().getRegistrationId(); //google 등
            ProviderType provider = ProviderType.from(providerStr);
            String email = (String) oAuth2User.getAttributes().get("email");

            if (email == null) {
                throw new OAuth2AuthenticationException("이메일이 존재하지 않음");
            }

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                // 연동 처리: local 계정이면 소셜 연동 유도
                if (user.getProvider() == ProviderType.LOCAL && provider != ProviderType.LOCAL) {
                    log.warn("기존 local계정 존재: 소셜 연동 유도");
                    throw new OAuth2AuthenticationException("이미 일반 회원으로 가입된 이메일입니다. 연동하시겠습니까?");
                }

                // 이미 해당 소셜로 연동된 경우
                if (user.getProvider() == provider) {
                    return UserPrincipal.create(user, oAuth2User.getAttributes());
                }

                // 다른 소셜로 이미 연동된 경우
                throw new OAuth2AuthenticationException("이메일이 다른 소셜 계정과 이미 연동되어 있습니다.");
            }

            // db에 사용자 없으면 생성
            user = userRepository.save(
                    User.builder()
                            .email(email)
                            .provider(provider)
                            .roles(Set.of("ROLE_USER"))
                            .build()
            );

            return UserPrincipal.create(user, oAuth2User.getAttributes());
        } catch (OAuth2AuthenticationException e) {
            throw e; //던진메세지는 프론트에서 잡을수있도록 그대로 전달
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("사용자 정보 로딩중 예외");
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ CustomOAuth2UserService 빈 등록 완료");
    }

}
