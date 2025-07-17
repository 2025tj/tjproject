package com.tj.tjp.config.oauth2;

import com.tj.tjp.dto.oauth2.StateInfo;
import com.tj.tjp.util.CryptoUtils;
import com.tj.tjp.util.OAuth2StateEncoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
//    private final CryptoUtils cryptoUtils;
    private final OAuth2StateEncoder stateEncoder;

    public CustomAuthorizationRequestResolver(
            ClientRegistrationRepository repo,
            String baseUri,
            OAuth2StateEncoder stateEncoder) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, baseUri);
        this.stateEncoder = stateEncoder;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customize(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customize(req, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        String mode = request.getParameter("mode");
        String token = request.getParameter("token"); // 🔥 토큰 파라미터 추가

        if (mode != null || token != null) {
            try {
                // State 정보 구성
                // StateInfo 객체 + 암호화
                StateInfo stateInfo = StateInfo.builder()
                        .mode(mode)
                        .token(token)
                        .originalState(req.getState())
                        .timestamp(System.currentTimeMillis())
                        .build();

                // State 정보를 JSON으로 변환 후 암호화
                String stateJson = stateInfo.toJson();
//                String encryptedState = cryptoUtils.encrypt(stateJson);
                String encryptedState = stateEncoder.encrypt(stateJson);

                log.debug("OAuth2 State 암호화 완료: mode={}, token={}", mode, token != null ? "***" : null);

                return OAuth2AuthorizationRequest.from(req)
                        .state(encryptedState)
                        .build();

            } catch (Exception e) {
                log.error("OAuth2 State 암호화 실패", e);
                // 암호화 실패 시 기본 동작 수행
                return createLegacyState(req, mode);
            }
        }
        return req;
    }

    /**
     * 기존 방식 (하위 호환성)
     */
    private OAuth2AuthorizationRequest createLegacyState(OAuth2AuthorizationRequest req, String mode) {
        if ("link".equals(mode)) {
            return OAuth2AuthorizationRequest.from(req)
                    .state(req.getState() + ":link")  // 기존 방식 유지
                    .build();
        }
        return req;
    }

//    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req, HttpServletRequest request) {
//        if (req == null) return null;
//        String mode = request.getParameter("mode");
//        if (mode != null) {
//            // 기존 state를 안전하게 보존
//            String origState = req.getState();
//            String state = "mode=" + mode + ":" + origState;
//            return OAuth2AuthorizationRequest.from(req)
//                    .state(state)
//                    .build();
//        }
//        return req;
//    }
}
