package com.tj.tjp.security.service;

import com.tj.tjp.entity.user.ProviderType;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.security.principal.BlockedOAuth2UserPrincipal;
import com.tj.tjp.security.principal.LinkableOAuth2UserPrincipal;
import com.tj.tjp.security.principal.OAuth2UserPrincipal;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(request);
            Map<String, Object> attributes = oAuth2User.getAttributes();

            String providerName = request.getClientRegistration().getRegistrationId(); // e.g., google, kakao
            ProviderType provider = ProviderType.from(providerName);

            String email = (String) attributes.get("email");
            if (email == null || email.isBlank()) {
                log.warn("[OAuth2UserService] 소셜 로그인에 이메일 정보 없음 → 차단");
                throw new OAuth2AuthenticationException("이메일이 제공되지 않았습니다.");
            }

            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // ✅ 소셜 제공자 불일치 → 연동 가능 여부 체크
                if (user.getProvider() != provider) {
                    if (user.getProvider() == ProviderType.LOCAL) {
                        log.warn("✅ LOCAL 계정 존재 → 연동 유도");
                        return new LinkableOAuth2UserPrincipal(user, attributes);
                    } else {
                        log.warn("❌ 이미 다른 소셜로 연동됨 ({} → {})", user.getProvider(), provider);
                        return new BlockedOAuth2UserPrincipal(user, attributes, "다른 소셜 계정으로 이미 연동되어 있습니다.");
                    }
                }

                // ✅ 이미 해당 provider와 연동된 사용자
                return new OAuth2UserPrincipal(user, attributes);
            }

            // db에 사용자 없으면 생성
            User newUser = userRepository.save(
                    User.builder()
                            .email(email)
                            .provider(provider)
                            .roles(Set.of("ROLE_USER"))
                            .build()
            );

            return new OAuth2UserPrincipal(newUser, attributes);

        } catch (OAuth2AuthenticationException e) {
            log.error("❌ OAuth2 인증 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 사용자 정보 로딩 중 예외 발생: {}", e.getMessage(), e);
            return new BlockedOAuth2UserPrincipal(null, Map.of(), "사용자 정보 로딩 중 오류 발생");
        }
    }
}
