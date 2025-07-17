package com.tj.tjp.security.service;

import com.tj.tjp.entity.user.SocialAccount;
import com.tj.tjp.entity.user.User;
import com.tj.tjp.exception.OAuth2SignupRequiredException;
import com.tj.tjp.repository.user.SocialAccountRepository;
import com.tj.tjp.repository.user.UserRepository;
import com.tj.tjp.security.oauth.strategy.OAuth2ProviderStrategy;
import com.tj.tjp.security.principal.AuthenticatedUser;
import com.tj.tjp.security.principal.OAuth2UserPrincipal;
import com.tj.tjp.security.service.TokenService;
import com.tj.tjp.service.SocialAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountService socialAccountService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SocialAccountRepository socialAccountRepository;
    private final Map<String, OAuth2ProviderStrategy> providerStrategies;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        //1. 기본 정보 로드
        OAuth2User oauth2User = super.loadUser(request);
        Map<String, Object> attributes = oauth2User.getAttributes();

        // 2. provider 정보 추출
        String provider= request.getClientRegistration().getRegistrationId();
        OAuth2ProviderStrategy strategy = providerStrategies.get(provider);

        if (strategy ==null) {
            throw new IllegalArgumentException("Unsupported provider: "+ provider);
        }

        // 3. providerId, email 등 파싱
        String providerId = strategy.extractProviderId(attributes);
        String email = strategy.extractEmail(attributes);

        log.info("OAuth2 로그인 시작: provider={}, email={}", provider, email);

        // 4. 연동 요청인지 확인
        boolean isLinkMode = isLinkMode(request);
//        boolean isLinkMode = isLinkMode();

        if (isLinkMode) {
            log.info("[OAuth2 연동 모드] 로그인 중인 유저에게 소셜 계정 연결 시도 중");

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth != null && currentAuth.getPrincipal() instanceof OAuth2UserPrincipal principal) {
                User currentUser = principal.getUser();

                // 이미 연동된 계정인지 검사
                Optional<SocialAccount> existing = socialAccountRepository.findByProviderAndProviderId(provider, providerId);
                if (existing.isPresent()) {
                    throw new IllegalStateException("이미 다른 계정에 연동된 소셜 계정입니다.");
                }

                // 연동 저장
                SocialAccount socialAccount = SocialAccount.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .user(currentUser)
                        .build();
                socialAccountRepository.save(socialAccount);

                return new OAuth2UserPrincipal(currentUser, oauth2User.getAttributes());
            } else {
                throw new IllegalStateException("현재 로그인된 사용자가 없습니다 (연동 실패)");
            }
        }

        //5. 일반 로그인 플로우
        return socialLoginFlow(provider, providerId, email, oauth2User);

    }


//    private boolean isLinkMode() {
//        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (attr == null) return false;
//
//        HttpServletRequest request = attr.getRequest();
//        String mode = request.getParameter("mode");
//        return "link".equals(mode);
//    }

    private boolean isLinkMode(OAuth2UserRequest request) {
        // 디버깅을 위한 상세 로그
        log.info("=== isLinkMode 디버깅 시작 ===");
        log.info("AdditionalParameters: {}", request.getAdditionalParameters());

        // HttpServletRequest에서 state 파라미터 직접 확인
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest httpRequest = attr.getRequest();
                String state = httpRequest.getParameter("state");
                log.info("HttpServletRequest state: {}", state);

                if (state != null && state.startsWith("mode=link")) {
                    log.info("✅ 연동 모드 감지됨!");

                    extractAndSetAuthentication(state);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("state 파라미터 확인 중 오류: {}", e.getMessage());
        }

        log.info("❌ 일반 로그인 모드");
        return false;
    }

    private void extractAndSetAuthentication(String state) {
        try {
            // state에서 token 추출: "mode=link&token=eyJ..."
            if (state.contains("&token=")) {
                String tokenPart = state.substring(state.indexOf("&token=") + 7);
                String token = tokenPart.split(":")[0]; // ":원본state" 제거

                // JWT 토큰 검증 및 사용자 정보 추출
                if (tokenService.validateAccessToken(token)) {
                    String email = tokenService.getEmailFromAccessToken(token);
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        // SecurityContext에 인증 정보 설정
                        OAuth2UserPrincipal principal = new OAuth2UserPrincipal(user, Map.of());
                        Authentication auth = new OAuth2AuthenticationToken(
                                principal,
                                List.of(),
                                "temp"
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("✅ 토큰을 통한 사용자 인증 설정 완료: {}", email);
                    }
                }
            }
        } catch (Exception e) {
            log.error("토큰 추출 및 인증 설정 실패: {}", e.getMessage());
        }
    }


//    private boolean isLinkMode(OAuth2UserRequest request) {
//        Object stateObj = request.getAdditionalParameters().get("state");
//        if (stateObj instanceof String state) {
//            return state.startsWith("mode=link");
//        }
//        return false;
//    }

    private OAuth2User socialLoginFlow(String provider, String providerId, String email, OAuth2User oAuth2User) {
        Optional<SocialAccount> socialOpt = socialAccountRepository.findByProviderAndProviderId(provider, providerId);

        if (socialOpt.isPresent()) {
            User user = socialOpt.get().getUser();
            return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
        } else {
            log.info("소셜 계정 회원가입 필요: {} ({})", email, provider);
            throw new OAuth2SignupRequiredException(email, provider, providerId);
        }
    }
}

//    @Transactional
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
//        try {
//            // 1) 액세스 토큰으로 프로바이더 사용자 정보 조회
//            OAuth2User oAuth2User = super.loadUser(request);
//
//            // 2) 프로바이더 정보 추출
//            String provider = request.getClientRegistration().getRegistrationId();
//            Map<String, Object> attrs = oAuth2User.getAttributes();
//
//            log.info("OAuth2 로그인 시작: provider={}, email={}", provider, attrs.get("email"));
//
//            // 3) 이메일 추출 (필수)
//            String email = Optional.ofNullable((String) attrs.get("email"))
//                    .filter(e -> !e.isBlank())
//                    .orElseThrow(() -> new OAuth2AuthenticationException(
//                            new OAuth2Error("EMAIL_NOT_FOUND", "이메일이 제공되지 않았습니다.", null)
//                    ));
//
//            // 4) 프로바이더별 고유 ID 추출
//            String providerId = socialAccountService.extractProviderId(provider, attrs);
//
//            // 5) 현재 HTTP 요청에서 Authorization 헤더 확인 (마이페이지 연동 감지)
//            User currentUser = getCurrentUserFromRequest();
//
////            // State 파라미터에서 모드 확인
////            String state = getCurrentState(request);
////            boolean isLinkMode = state != null && state.contains("mode=link");
//
//            if (currentUser != null) {
//                log.info("마이페이지 소셜 연동 시도: 현재사용자={}, 연동이메일={}", currentUser.getEmail(), email);
//
//                // 이미 다른 사용자에게 연동된 소셜 계정인지 확인
//                if (socialAccountService.isLinked(provider, providerId)) {
//                    if (!socialAccountService.isLinkedToUser(provider, providerId, currentUser)) {
//                        throw new OAuth2AuthenticationException(
//                                new OAuth2Error("ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다.", null)
//                        );
//                    }
//                }
//
//                // 현재 로그인된 사용자에게 소셜 계정 연동
//                socialAccountService.linkSocialAccount(currentUser, provider, providerId);
//                log.info("마이페이지 소셜 계정 연동 완료: {} → {} ({})",
//                        currentUser.getEmail(), provider, providerId);
//                return new OAuth2UserPrincipal(currentUser, attrs);
//            }
//
//            // 6) 일반 소셜 로그인: 연동된 계정 있으면 → 로그인 처리
//            if (socialAccountService.isLinked(provider, providerId)) {
//                User user = socialAccountService.findUserByProviderAndProviderId(provider, providerId);
//                log.info("소셜 계정 로그인 처리: {} → {} ({})", user.getEmail(), provider, providerId);
//                return new OAuth2UserPrincipal(user, attrs);
//            }
//
//            // 7) 연동된 계정 없으면 → 회원가입 페이지로 넘김
//            log.info("소셜 계정 회원가입 필요: {} ({})", email, provider);
//            throw new OAuth2SignupRequiredException(email, provider, providerId);
//
//        } catch (Exception e) {
//            log.error("OAuth2 로그인 실패: {}", e.getMessage(), e);
//            throw e;
//        }
//    }



//    /**
//     * 현재 HTTP 요청에서 Authorization 헤더를 통해 현재 사용자 확인
//     */
//    private User getCurrentUserFromRequest() {
//        try {
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (attributes == null) return null;
//
//            HttpServletRequest httpRequest = attributes.getRequest();
//            String authHeader = httpRequest.getHeader("Authorization");
//
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//
//                if (tokenService.validateAccessToken(token)) {
//                    String email = tokenService.getEmailFromAccessToken(token);
//                    return userRepository.findByEmail(email).orElse(null);
//                }
//            }
//
//            return null;
//        } catch (Exception e) {
//            log.warn("현재 사용자 확인 실패: {}", e.getMessage());
//            return null;
//        }
//    }
//}

//package com.tj.tjp.security.service;
//
//import com.tj.tjp.entity.user.ProviderType;
//import com.tj.tjp.entity.user.SocialAccount;
//import com.tj.tjp.entity.user.User;
//import com.tj.tjp.exception.OAuth2LinkRequiredException;
//import com.tj.tjp.exception.OAuth2SignupRequiredException;
//import com.tj.tjp.repository.user.SocialAccountRepository;
//import com.tj.tjp.repository.user.UserRepository;
//import com.tj.tjp.security.principal.AuthenticatedUser;
//import com.tj.tjp.security.principal.BlockedOAuth2UserPrincipal;
//import com.tj.tjp.security.principal.LinkableOAuth2UserPrincipal;
//import com.tj.tjp.security.principal.OAuth2UserPrincipal;
//import com.tj.tjp.service.SocialAccountService;
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.OAuth2Error;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private final @Lazy SocialAccountService socialAccountService;
//    private final UserRepository userRepository;
//    private final TokenService tokenService;
//
//
//    @Transactional
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
//        // 1) super.loadUser를 통해 액세스 토큰으로 프로바이더 사용자 정보(CLAIMS 포함) 조회
//        OAuth2User oAuth2User = super.loadUser(request);
//
//        // 2) 프로바이더 ID(google, kakao 등)와 전달 받은 속성(attributes) 확보
//        String provider = request.getClientRegistration().getRegistrationId(); // google, kakao, etc
//        Map<String, Object> attrs = oAuth2User.getAttributes();
//
//        // 3) 이메일은 필수 값으로, 스코프에 포함되어 있지 않으면 예외 처리
//        String email = Optional.ofNullable((String) attrs.get("email"))
//                .filter(e -> !e.isBlank())
//                .orElseThrow(() -> new OAuth2AuthenticationException(
//                        new OAuth2Error("EMAIL_NOT_FOUND", "이메일이 제공되지 않았습니다.", null)
//                ));
//
//        // 4) 각 프로바이더별 고유 식별자(providerId) 추출 로직 위임
//        String providerId = socialAccountService.extractProviderId(provider, attrs);
//
//        // 5) 현재 HTTP 요청에서 Authorization 헤더 확인 (마이페이지 연동 감지)
//        User currentUser = getCurrentUserFromRequest();
//
//        if (currentUser != null) {
//            log.info("마이페이지 소셜 연동 시도: 현재사용자={}, 연동이메일={}", currentUser.getEmail(), email);
//
//            // 이미 다른 사용자에게 연동된 소셜 계정인지 확인
//            if (socialAccountService.isLinked(provider, providerId)) {
//                if (!socialAccountService.isLinkedToUser(provider, providerId, currentUser)) {
//                    throw new OAuth2AuthenticationException(
//                            new OAuth2Error("ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다.", null)
//                    );
//                }
//            }
//
//            // 현재 로그인된 사용자에게 소셜 계정 연동
//            socialAccountService.linkSocialAccount(currentUser, provider, providerId);
//            log.info("마이페이지 소셜 계정 연동 완료: {} → {} ({})",
//                    currentUser.getEmail(), provider, providerId);
//            return new OAuth2UserPrincipal(currentUser, attrs);
//        }
//
//        // 6) 일반 소셜 로그인: 연동된 계정 있으면 → 로그인 처리
//        if (socialAccountService.isLinked(provider, providerId)) {
//            User user = socialAccountService.findUserByProviderAndProviderId(provider, providerId);
//            log.info("소셜 계정 로그인 처리: {} → {} ({})", user.getEmail(), provider, providerId);
//            return new OAuth2UserPrincipal(user, attrs);
//        }
//
//        // 7) 연동된 계정 없으면 → 회원가입 페이지로 넘김
//        log.info("소셜 계정 회원가입 필요: {} ({})", email, provider);
//        throw new OAuth2SignupRequiredException(email, provider, providerId);
//
//    } catch (Exception e) {
//        log.error("OAuth2 로그인 실패: {}", e.getMessage(), e);
//        throw e;
//    }
//}
//
////        // !) state에서 mode=link 여부 판별
////        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
////        String state = httpRequest.getParameter("state");
////        boolean isLink = state != null && state.startsWith("mode=link:");
////
////        if (isLink) {
////            // 🎯 연동 플로우: 반드시 현재 로그인된 유저 + 소셜 계정만 연동
////            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
////            if (currentAuth != null &&
////                    currentAuth.isAuthenticated() &&
////                    currentAuth.getPrincipal() instanceof AuthenticatedUser currentUser) {
////
////                // 이미 다른 사용자에게 연동된 소셜 계정인지 체크
////                if (socialAccountService.isLinked(provider, providerId)) {
////                    if (!socialAccountService.isLinkedToUser(provider, providerId, currentUser.getUser())) {
////                        throw new OAuth2AuthenticationException(
////                                new OAuth2Error("ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다.", null)
////                        );
////                    }
////                } else {
////                    // 실제로 소셜 계정 연동 처리 (없을 때만)
////                    socialAccountService.linkSocialAccount(currentUser.getUser(), provider, providerId);
////                    log.info("마이페이지 소셜 계정 연동 완료: {} → {} ({})",
////                            currentUser.getEmail(), provider, providerId);
////                }
////                return new OAuth2UserPrincipal(currentUser.getUser(), attrs);
////            } else {
////                // 비로그인 상태에서 연동 시도(비정상) → 예외
////                throw new OAuth2AuthenticationException("로그인 상태에서만 연동 가능합니다.");
////            }
////        }
////        // 5) 마이페이지에서 소셜 연동인 경우 처리 (이메일 상관없이 현재 로그인 사용자에게 연동)
////        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
////        if (currentAuth != null &&
////                currentAuth.getPrincipal() instanceof AuthenticatedUser &&
////                currentAuth.isAuthenticated()) {
////            AuthenticatedUser currentUser = (AuthenticatedUser) currentAuth.getPrincipal();
////
////            // 이미 다른 사용자에게 연동된 소셜 계정인지 확인
////            if (socialAccountService.isLinked(provider, providerId)) {
////                if (!socialAccountService.isLinkedToUser(provider, providerId, currentUser.getUser())) {
////                    throw new OAuth2AuthenticationException(
////                            new OAuth2Error("ALREADY_LINKED", "이미 다른 계정에 연동된 소셜 계정입니다.", null)
////                    );
////                }
////            }
////
////            // 현재 로그인된 사용자에게 소셜 계정 연동
////            socialAccountService.linkSocialAccount(currentUser.getUser(), provider, providerId);
////            log.info("마이페이지 소셜 계정 연동 완료: {} → {} ({})",
////                    currentUser.getEmail(), provider, providerId);
////            return new OAuth2UserPrincipal(currentUser.getUser(), attrs);
////        }
//
////        // 6) 일반 소셜 로그인: 연동된 계정 있으면 → 로그인 처리
////        if(socialAccountService.isLinked(provider, providerId)) {
////            User user = socialAccountService.findUserByProviderAndProviderId(provider, providerId);
////            log.info("소셜 계정 로그인 처리: {} → {} ({})", user.getEmail(), provider, providerId);
////            return new OAuth2UserPrincipal(user, attrs);
////        }
////
////        // 7) 연동된 계정 없으면 → 회원가입 페이지로 넘김
////        log.info("소셜 계정 회원가입 필요: {} ({})",email,provider);
////        throw new OAuth2SignupRequiredException(email, provider, providerId);
////    }
////}
//
////        // 5) 이미 연동된 소셜 계정이 있는지 확인 -> 로그인
////        if (socialAccountService.existsByProviderAndProviderId(provider, providerId)) {
////            // 기존 소셜 계정과 연결된 로컬 사용자 정보를 조회
////            User user = socialAccountService.findUserByProviderAndProviderId(provider, providerId);
////            // 즉시 로그인 처리
////            return new OAuth2UserPrincipal(user, attrs);
////        }
////
////        // 6) 소셜 계정 연동 기록이 없지만 동일한 이메일의 로컬 계정이 있으면 -> 연동 필요 예외
////        if (socialAccountService.existsLocalUserByEmail(email)) {
////            // 보안을 위해 단순 자동 연동 대신, 사용자 확인(로컬 로그인)을 유도하는 예외 발생
////            throw new OAuth2LinkRequiredException(email, provider);
////        }
////
////        // 3) 완전 신규 사용자 → “회원가입 필요” 예외
////        throw new OAuth2SignupRequiredException(email, provider);
////
//////        // 7) 신규 가입 및 소셜 계정 정보 저장
//////        User newUser = socialAccountService.registerSocialUser(email);
//////        socialAccountService.linkIfAbsent(newUser, provider, attrs);
//////
//////        // 8) 최종 로그인 처리 (JWT 발급은 SuccessHandler에서)
//////        return new OAuth2UserPrincipal(newUser, attrs);
////    }
////}
