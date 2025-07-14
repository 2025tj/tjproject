package com.tj.tjp.config.sercurity;

import com.tj.tjp.security.handler.OAuth2SuccessHandler;
import com.tj.tjp.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@RequiredArgsConstructor
public class Oauth2SecurityConfig {

    // CustomOAuth2UserService: OAuth2 로그인 시 User 정보를 가져와
    // 우리 시스템의 User 엔티티로 매핑·저장하는 서비스
    private final CustomOAuth2UserService customOAuth2UserService;

    // OAuth2 로그인 성공 후 호출되는 핸들러
    // 여기서 JWT 토큰을 발급하고, 프론트엔드로 리디렉트합니다
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // 로그인 실패 시 호출할 핸들러가 필요하면 주석 해제 후 사용하세요
    // private final OAuth2FailureHandler oAuth2FailureHandler;


    /**
     * OAuth2 로그인 설정을 구성하는 메서드입니다.
     * @param http HttpSecurity 설정 객체
     * @throws Exception 설정 중 예외 발생 시 던집니다
     */
    public void configureOauth2(HttpSecurity http) throws Exception {
        http
            // OAuth2 로그인 기능 활성화
            .oauth2Login(oauth2 -> oauth2
                // 1) 사용자 인증 요청을 보낼 엔드포인트 설정
                .authorizationEndpoint(auth -> auth
                    // 클라이언트가 /oauth2/authorization/{provider}로 요청
                    .baseUri("/oauth2/authorization")
                )
                // 2) 인증 후 리디렉션을 받을 엔드포인트 설정
                .redirectionEndpoint(redir -> redir
                    // 예: /oauth2/callback/google
                    .baseUri("/oauth2/callback/*")
                )
                // 3) OAuth2 프로바이더에서 사용자 정보 가져올 서비스 등록
                .userInfoEndpoint(ui -> ui
                    .userService(customOAuth2UserService)
                )
                // 4) 로그인 성공 후 핸들러 등록
                .successHandler(oAuth2SuccessHandler)
                // 5) 로그인 실패 시 핸들러 등록 (필요 시 사용)
//                .failureHandler(oAuth2FailureHandler)
            );
    }
}
