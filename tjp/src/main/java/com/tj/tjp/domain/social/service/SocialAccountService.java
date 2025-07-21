package com.tj.tjp.domain.social.service;

import com.tj.tjp.domain.social.entity.SocialAccount;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.common.exception.ResourceNotFoundException;
import com.tj.tjp.domain.social.repository.SocialAccountRepository;
import com.tj.tjp.domain.user.repository.UserRepository;
import com.tj.tjp.domain.auth.security.jwt.OneTimeTokenProvider;
import com.tj.tjp.domain.oauth2.strategy.OAuth2ProviderStrategy;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountService {
    private final Map<String, OAuth2ProviderStrategy> providerStrategies;
    private final SocialAccountRepository socialAccountRepository;
    private final OAuth2AuthorizedClientService authorizedClientService; // provider metadata
    private final UserRepository userRepository;
    private final OneTimeTokenProvider oneTimeTokenProvider;

    /**
     * 소셜 프로바이더의 고유 ID 추출
     */
    public String extractProviderId(String provider, Map<String, Object> attrs) {
        OAuth2ProviderStrategy strategy = providerStrategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        }
        return strategy.extractProviderId(attrs);
    }

    /**
     * 소셜 계정 조회 (Optional)
     */
    public Optional<SocialAccount> findSocialAccount(String provider, String providerId) {
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * 소셜 계정으로 연동된 사용자 조회
     */
    public User findUserByProviderAndProviderId(String provider, String providerId) {
        return findSocialAccount(provider, providerId)
                .map(SocialAccount::getUser)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "연동된 소셜 계정이 없습니다: " + provider + "/" + providerId));
    }

    /**
     * 소셜 계정 연동 여부 확인
     */
    public boolean isLinked(String provider, String providerId) {
        return findSocialAccount(provider, providerId).isPresent();
    }

    /**
     * 소셜 계정이 특정 사용자에게 연동되어 있는지 확인
     */
    public boolean isLinkedToUser(String provider, String providerId, User user) {
        return findSocialAccount(provider, providerId)
                .map(account -> account.getUser().getId().equals(user.getId()))
                .orElse(false);
    }

    /**
     * 사용자에게 소셜 계정 연동
     */
    @Transactional
    public void linkSocialAccount(User user, String provider, String providerId) {
        // 이미 연동된 소셜 계정인지 확인
        Optional<SocialAccount> existingAccount = findSocialAccount(provider, providerId);

        if (existingAccount.isPresent()) {
            SocialAccount account = existingAccount.get();
            if (account.getUser().getId().equals(user.getId())) {
                log.info("이미 연동된 소셜 계정: {} → {} ({})", user.getEmail(), provider, providerId);
                return;
            } else {
                throw new IllegalStateException("이미 다른 사용자에게 연동된 소셜 계정입니다.");
            }
        }

        // 새로운 소셜 계정 연동
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .build();

        socialAccountRepository.save(socialAccount);
        log.info("소셜 계정 연동 완료: {} → {} ({})", user.getEmail(), provider, providerId);
    }

    /**
     * 소셜 계정 연동 해제
     */
    @Transactional
    public void unlinkSocialAccount(User user, String provider) {
        long deleted = socialAccountRepository.deleteByUserAndProvider(user, provider);
        if (deleted == 0) {
            throw new ResourceNotFoundException("연동된 소셜 계정이 없습니다: " + provider);
        }
        log.info("소셜 계정 연동 해제: {} → {}", user.getEmail(), provider);
    }

    /**
     * 회원가입 대기 중인 소셜 정보 조회
     */
    public Optional<SocialSignupInfo> getPendingSignup(String token) {
        if (token == null) return Optional.empty();

        try {
            String email = oneTimeTokenProvider.extractEmail(token);
            String provider = oneTimeTokenProvider.extractProvider(token);
            String providerId = oneTimeTokenProvider.extractProviderId(token);

            return Optional.of(new SocialSignupInfo(email, provider, providerId));
        } catch (JwtException ex) {
            log.warn("유효하지 않은 회원가입 토큰: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 사용자의 연동된 소셜 프로바이더 목록 조회
     */
    public List<String> getLinkedProviders(User user) {
        return socialAccountRepository.findByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();
    }

    /**
     * 회원가입 대기 정보 DTO
     */
    public record SocialSignupInfo(String email, String provider, String providerId) {}
}

    /**
     * 이미 로그인된 상태에서 소셜 계정(provider)만 새로 연동합니다.
     * @param user        현재 로그인된 User 엔티티
     * @param provider    "google", "kakao" 등
     * @param auth        SecurityContext의 Authentication
     */
//    public void link(User user, String provider, Authentication auth) {
//        // 1. 사용자가 이전에 로그인하고 남긴 OAuth2AuthorizedClient 꺼내기
//        OAuth2AuthorizedClient client =
//                authorizedClientService.loadAuthorizedClient(
//                        provider,
//                        auth.getName()    // principal name (보통 이메일)
//                );
//        if (client == null) {
//            throw new IllegalStateException("OAuth2 client not found for provider " + provider);
//        }
//
//        // 2. 저장된 AccessToken 으로 사용자 정보 요청
//        OAuth2UserRequest userRequest = new OAuth2UserRequest(
//                client.getClientRegistration(),
//                client.getAccessToken()
//        );
//        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
//        OAuth2User oauth2User = defaultOAuth2UserService.loadUser(userRequest);
//        Map<String,Object> attributes = oauth2User.getAttributes();
//
//        // 3. 실제 연동 로직 (중복 검사 + 저장)
//        linkIfAbsent(user, provider, attributes);
//    }

//    /** 소셜 연동 해제 */
//    public void unlink(User user, String provider) {
//        long deleted = socialAccountRepository.deleteByUserAndProvider(user, provider);
//        if (deleted == 0) {
//            // 연동된 계정이 없었으면, 예외 던지거나
//            throw new ResourceNotFoundException(
//                    "연동된 소셜 계정이 없습니다: " + provider
//            );
//        }
//    }

    /**
     * User + OAuth2UserRequest → providerId 추출 & 중복 검사 후 저장
     * (로그인 시엔 OAuth2UserRequest, 연동 시엔 OAuth2AuthorizedClient 로 뽑아낸 속성 Map 등)
     */
//    public void linkIfAbsent(User user, String provider, Map<String, Object> attributes) {
//        // providerId 추출 (Google/Kakao 대응)
//        String providerId = extractProviderId(provider, attributes);
//        // 이미 연동된 적 없으면 저장
//        socialAccountRepository.findByProviderAndProviderId(provider, providerId)
//                // 없으면 새로 만들어서 리턴
//                .orElseGet(() -> socialAccountRepository.save(
//                        SocialAccount.builder()
//                                .user(user)
//                                .provider(provider)
//                                .providerId(providerId)
//                                .build()
//                ));
//    }

//    /** 소셜 프로바이더의 고유 ID 추출 */
//    public String extractProviderId(String provider, Map<String,Object> attrs) {
//        OAuth2ProviderStrategy strategy = providerStrategies.get(provider);
//        if (strategy == null) {
//            throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
//        }
//        return strategy.extractProviderId(attrs);
//    }
//
//    /** provider+providerId 연동 여부 */
//    public boolean existsByProviderAndProviderId(String provider, String providerId) {
//        return socialAccountRepository.findByProviderAndProviderId(provider, providerId).isPresent();
//    }
//
//    /** provider+providerId로 연동된 사용자 조회 */
//    public User findUserByProviderAndProviderId(String provider, String providerId) {
//        return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
//                .map(SocialAccount::getUser)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "연동된 소셜 계정이 없습니다: " + provider + "/" + providerId
//                ));
//    }
//
//    /** 로컬 사용자 존재 여부(email) */
//    public boolean existsLocalUserByEmail(String email) {
//        return userRepository.findByEmail(email).isPresent();
//    }
//
//    /** 소셜 신규 사용자 등록(로컬 계정 생성) */
//    public User registerSocialUser(String email) {
//        User newUser = User.builder()
//                .email(email)
//                .emailVerified(true)
//                .status("ACTIVE")
//                .roles(Set.of("ROLE_USER"))
//                .build();
//        return userRepository.save(newUser);
//    }
//
//    /** 소셜 계정 연동 (중복 없을 때만 저장) */
//    public void linkIfAbsent(User user, String provider, Map<String, Object> attributes) {
//        // providerId 추출 (Google/Kakao 대응)
//        String providerId = extractProviderId(provider, attributes);
//        // 이미 연동된 적 없으면 저장
//        if (!existsByProviderAndProviderId(provider, providerId)) {
//            // 없으면 새로 만듬
//            socialAccountRepository.save(
//                    SocialAccount.builder()
//                            .user(user)
//                            .provider(provider)
//                            .providerId(providerId)
//                            .build()
//            );
//        }
//    }
//
//    public void linkWithToken(User user, String token) {
//        // 1. 토큰에서 email, provider 추출
//        String email = oneTimeTokenProvider.extractEmail(token);
//        String provider = oneTimeTokenProvider.extractProvider(token);
//
//        // 2. email 불일치 검증 (보안)
//        if (!user.getEmail().equals(email)) {
//            throw new SecurityException("로그인된 사용자와 이메일이 일치하지 않습니다");
//        }
//
//        // 3. Authentication 없이 연동 진행
//        OAuth2AuthorizedClient client =
//                authorizedClientService.loadAuthorizedClient(provider, user.getEmail());
//        if (client == null) throw new IllegalStateException("OAuth2 클라이언트 없음");
//
//        Map<String, Object> attributes = loadUserAttributes(client);
//
//        linkIfAbsent(user, provider, attributes);
//    }
//
//    public List<String> getLinkedProviders(User user) {
//        return socialAccountRepository.findByUser(user).stream()
//                .map(SocialAccount::getProvider)
//                .toList();
//    }
//
//    public void linkWithEmailAndProvider(User user, String email, String provider) {
//        String token = oneTimeTokenProvider.createToken(email, provider);
//        linkWithToken(user, token);
//    }
//
//    public Map<String, Object> loadUserAttributes(OAuth2AuthorizedClient client) {
//        OAuth2UserRequest request = new OAuth2UserRequest(client.getClientRegistration(), client.getAccessToken());
//        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);
//        return oAuth2User.getAttributes();
//    }
//
//    /** 도메인 객체로 pending link 정보 반환 */
//    public Optional<SocialLinkInfo> getPendingLink(String token) {
//        if (token == null) return Optional.empty();
//
//        try {
//            String email = oneTimeTokenProvider.extractEmail(token);
//            String provider = oneTimeTokenProvider.extractProvider(token);
//            return Optional.of(new SocialLinkInfo(email, provider));
//        } catch (JwtException ex) {
//            return Optional.empty();
//        }
//    }
//
//    /** 도메인 객체로 pending signup 정보 반환 */
//    public Optional<SocialLinkInfo> getPendingSignup(String token) {
//        if (token == null) return Optional.empty();
//        try {
//            String email    = oneTimeTokenProvider.extractEmail(token);
//            String provider = oneTimeTokenProvider.extractProvider(token);
//            return Optional.of(new SocialLinkInfo(email, provider));
//        } catch (JwtException ex) {
//            return Optional.empty();
//        }
//    }
//
//    /**
//     * providerId(소셜 고유 ID)를 직접 받아서 User에 SocialAccount를 생성합니다.
//     */
//    @Transactional
//    public void linkWithProviderId(User user, String provider, String providerId) {
//        // 이미 연동된 적 없다면 저장
//        if (!existsByProviderAndProviderId(provider, providerId)) {
//            socialAccountRepository.save(
//                    SocialAccount.builder()
//                            .user(user)
//                            .provider(provider)
//                            .providerId(providerId)
//                            .build()
//            );
//        }
//    }
//}
