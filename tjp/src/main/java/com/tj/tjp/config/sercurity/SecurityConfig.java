package com.tj.tjp.config.sercurity;

//import com.tj.tjp.security.CustomOAuth2FailureHandler;
//import com.tj.tjp.security.service.CustomOAuth2UserService;
import com.tj.tjp.security.filter.JwtAuthenticationFilter;
import com.tj.tjp.security.handler.OAuth2SuccessHandler;
//import com.tj.tjp.security.OAuth2UserCumstomService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final Oauth2SecurityConfig oauth2SecurityConfig;
    private final JwtSecurityConfig jwtSecurityConfig;
//    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // jwt 설정
        jwtSecurityConfig.configureJwt(http);

        //Oauth2 설정
        oauth2SecurityConfig.configureOauth2(http);

        // 공통 예외 처리, csrf, cors 등
        http
                // cors
                .cors(withDefaults())

                // csrf
                .csrf(csrf -> csrf.disable()) // csrf 비활성화

                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 1) HTTP 상태 401 (Unauthorized)
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            // 2) 응답을 JSON으로
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            // 3) CORS 요구사항 (React 개발 서버에 허용)
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                            response.setHeader("Access-Control-Allow-Credentials", "true");

                            // 4) 바디에 에러 메시지 쓰기
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("인증되지 않은 요청: {}", request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.getWriter().write("{\"error\":\"Access Denied\"}");
                        })
                );

        return http.build();
    }

    /**
     * AuthenticationManager 빈을 정의합니다.
     *
     * 스프링 시큐리티의 AuthenticationManager는
     * AuthenticationProvider를 통해 실제 인증 로직을 수행하는 핵심 컴포넌트입니다.
     *
     * AuthenticationConfiguration을 주입 받아,
     * 내부에 설정된 AuthenticationProvider 체인을 사용한 AuthenticationManager를 반환합니다.
     *
     * @param configuration 스프링이 제공하는 AuthenticationConfiguration
     * @return AuthenticationManager 인증 매니저 빈
     * @throws Exception 구성 중 예외 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
