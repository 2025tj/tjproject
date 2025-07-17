package com.tj.tjp.config.oauth2;

import com.tj.tjp.util.CryptoUtils;
import com.tj.tjp.util.OAuth2StateEncoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@RequiredArgsConstructor
@Configuration
public class OAuth2Config {

//    private final CryptoUtils cryptoUtils;
    private final OAuth2StateEncoder stateEncoder;

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        return new CustomAuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization",
//                cryptoUtils
                stateEncoder
        );
    }

    @Bean
    public DefaultOAuth2UserService defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }
}
//
//        return new OAuth2AuthorizationRequestResolver() {
//            private final DefaultOAuth2AuthorizationRequestResolver delegate =
//                    new DefaultOAuth2AuthorizationRequestResolver(
//                            clientRegistrationRepository, "/oauth2/authorization");
//
//            @Override
//            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
//                OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
//                return customizeAuthorizationRequest(authorizationRequest, request);
//            }
//
//            @Override
//            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
//                OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
//                return customizeAuthorizationRequest(authorizationRequest, request);
//            }
//
//            private OAuth2AuthorizationRequest customizeAuthorizationRequest(
//                    OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
//
//                if (authorizationRequest == null) {
//                    return null;
//                }
//
//                // mode 파라미터 확인
//                String mode = request.getParameter("mode");
//
//                if ("link".equals(mode)) {
//                    // state에 link 모드 정보 추가
//                    return OAuth2AuthorizationRequest.from(authorizationRequest)
//                            .state(authorizationRequest.getState() + ":link")
//                            .build();
//                }
//
//                return authorizationRequest;
//            }
//        };
//    }
//}