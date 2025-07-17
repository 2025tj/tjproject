//package com.tj.tjp.security.oauth.strategy;
//
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component("kakao")
//public class KakaoOAuth2ProviderStrategy implements OAuth2ProviderStrategy {
//    @Override
//    public String extractProviderId(Map<String, Object> attributes) {
//        Map<?, ?> account = (Map<?, ?>) attributes.get("kakao_account");
//        return String.valueOf(account.get("id"));
//    }
//}
