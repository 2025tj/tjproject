package com.tj.tjp.config.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String baseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, baseUri);
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

        if (mode != null) {
            String origState = req.getState();
            String state = "mode=" + mode;

            // 토큰이 있으면 state에 포함
            if (token != null) {
                state += "&token=" + token;
            }

            state += ":" + origState;

            return OAuth2AuthorizationRequest.from(req)
                    .state(state)
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
