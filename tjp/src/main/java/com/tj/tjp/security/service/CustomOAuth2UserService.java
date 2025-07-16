package com.tj.tjp.security.service;

import com.tj.tjp.entity.user.ProviderType;
import com.tj.tjp.entity.user.SocialAccount;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.exception.OAuth2LinkRequiredException;
import com.tj.tjp.exception.OAuth2SignupRequiredException;
import com.tj.tjp.repository.user.SocialAccountRepository;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.security.principal.BlockedOAuth2UserPrincipal;
import com.tj.tjp.security.principal.LinkableOAuth2UserPrincipal;
import com.tj.tjp.security.principal.OAuth2UserPrincipal;
import com.tj.tjp.service.SocialAccountService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final @Lazy SocialAccountService socialAccountService;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        // 1) super.loadUser를 통해 액세스 토큰으로 프로바이더 사용자 정보(CLAIMS 포함) 조회
        OAuth2User oAuth2User = super.loadUser(request);

        // 2) 프로바이더 ID(google, kakao 등)와 전달 받은 속성(attributes) 확보
        String provider = request.getClientRegistration().getRegistrationId(); // google, kakao, etc
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // 3) 이메일은 필수 값으로, 스코프에 포함되어 있지 않으면 예외 처리
        String email = Optional.ofNullable((String) attrs.get("email"))
                .filter(e -> !e.isBlank())
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("EMAIL_NOT_FOUND", "이메일이 제공되지 않았습니다.", null)
                ));

        // 4) 각 프로바이더별 고유 식별자(providerId) 추출 로직 위임
        String providerId = socialAccountService.extractProviderId(provider, attrs);

        // 5) 이미 연동된 소셜 계정이 있는지 확인 -> 로그인
        if (socialAccountService.existsByProviderAndProviderId(provider, providerId)) {
            // 기존 소셜 계정과 연결된 로컬 사용자 정보를 조회
            User user = socialAccountService.findUserByProviderAndProviderId(provider, providerId);
            // 즉시 로그인 처리
            return new OAuth2UserPrincipal(user, attrs);
        }

        // 6) 소셜 계정 연동 기록이 없지만 동일한 이메일의 로컬 계정이 있으면 -> 연동 필요 예외
        if (socialAccountService.existsLocalUserByEmail(email)) {
            // 보안을 위해 단순 자동 연동 대신, 사용자 확인(로컬 로그인)을 유도하는 예외 발생
            throw new OAuth2LinkRequiredException(email, provider);
        }

        // 3) 완전 신규 사용자 → “회원가입 필요” 예외
        throw new OAuth2SignupRequiredException(email, provider);

//        // 7) 신규 가입 및 소셜 계정 정보 저장
//        User newUser = socialAccountService.registerSocialUser(email);
//        socialAccountService.linkIfAbsent(newUser, provider, attrs);
//
//        // 8) 최종 로그인 처리 (JWT 발급은 SuccessHandler에서)
//        return new OAuth2UserPrincipal(newUser, attrs);
    }
}
