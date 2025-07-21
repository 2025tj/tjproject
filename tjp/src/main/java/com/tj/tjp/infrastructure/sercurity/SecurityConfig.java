package com.tj.tjp.infrastructure.sercurity;

//import com.tj.tjp.security.CustomOAuth2FailureHandler;
//import com.tj.tjp.security.service.CustomOAuth2UserService;
import com.tj.tjp.domain.auth.security.filter.JwtAuthenticationFilter;
import com.tj.tjp.domain.oauth2.handler.CustomOAuth2FailureHandler;
import com.tj.tjp.domain.oauth2.handler.CustomOAuth2SuccessHandler;
//import com.tj.tjp.security.OAuth2UserCumstomService;
import com.tj.tjp.domain.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final CorsConfigurationSource corsConfigurationSource;
//    @Autowired
//    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

//        // baseUri는 보통 "/oauth2/authorization"
//        CustomAuthorizationRequestResolver customResolver =
//                new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        // 공통 예외 처리, csrf, cors 등
        return http
                // cors 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // csrf 비활성화

                .csrf(CsrfConfigurer::disable) // csrf 비활성화

                // 엔드포인트별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        //    - "/api/users/me" 경로는 인증된 사용자만 접근 허용
                        .requestMatchers(
                                "/api/users/me",
                                "/api/users/pending-social-link",
                                "/api/email/resend-verification",
                                "/oauth2/link-complete/**"
                        ).authenticated()
                        //    - "/api/auth/**" 경로는 모두 허용 (회원가입·로그인 등)
                        .requestMatchers(
                                "/login/oauth2/code/**",
                                "/oauth2/authorization/**",
                                "/api/email/verify",
                                "/oauth2/**",
                                "/api/auth/**",
                                "/api/auth/signup",
                                "/api/auth/verify",
                                "/api/social/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/auth/password/reset-request"
                        ).permitAll()
                        //    - 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 세션 관리 설정: 세션을 사용하지 않고 JWT 기반 무상태(stateless)로 처리
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //jwt 설정
                // JWT 인증 필터 등록
                // - UsernamePasswordAuthenticationFilter 전에 실행되어
                //   요청 헤더(또는 쿠키)에서 토큰을 추출하고 검증함
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                //Oauth2 설정
                // OAuth2 로그인 기능 활성화
                .oauth2Login(oauth2 -> oauth2
                                // 1) 사용자 인증 요청을 보낼 엔드포인트 설정
                                .authorizationEndpoint(auth -> auth
                                                // 클라이언트가 /oauth2/authorization/{provider}로 요청
                                                .baseUri("/oauth2/authorization")
                                                // 암호화된 state를 지원하는 커스텀 resolver 사용
                                                .authorizationRequestResolver(authorizationRequestResolver)
//                    .authorizationRequestResolver(customResolver)
                                )
                                // 2) 인증 후 리디렉션을 받을 엔드포인트 설정
                                .redirectionEndpoint(redir -> redir
                                        // 예: /oauth2/callback/google
                                        .baseUri("/login/oauth2/code/*")
                                )
                                // 3) OAuth2 프로바이더에서 사용자 정보 가져올 서비스 등록
                                .userInfoEndpoint(ui -> ui
                                        .userService(customOAuth2UserService)
                                )
                                // 4) 로그인 성공 후 핸들러 등록
                                .successHandler(customOAuth2SuccessHandler)
//                                .loginProcessingUrl("/login/oauth2/code/*")
                                // 5) 로그인 실패 시 핸들러 등록 (필요 시 사용)
                                .failureHandler(customOAuth2FailureHandler)
                )

                //예외처리
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.debug("Authentication failed: {}", authException.getMessage());
                            // 1) HTTP 상태 401 (Unauthorized)
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            // 2) 응답을 JSON으로
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            // 3) CORS 요구사항 (React 개발 서버에 허용)
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                            response.setHeader("Access-Control-Allow-Credentials", "true");

                            // 4) 바디에 에러 메시지 쓰기
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.debug("Access denied: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .build();
    }

    /**
     * AuthenticationManager 빈을 정의합니다.
     * <p>
     * 스프링 시큐리티의 AuthenticationManager는
     * AuthenticationProvider를 통해 실제 인증 로직을 수행하는 핵심 컴포넌트입니다.
     * <p>
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

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:5173")); // 또는 프론트 주소
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setExposedHeaders(List.of("Access-Token", "Refresh-Token"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }

//    @Bean
//    public DefaultOAuth2UserService defaultOAuth2UserService() {
//        return new DefaultOAuth2UserService();
//    }

//    // 암호화된 state 지원하는 OAuth2AuthorizationRequestResolver를 직접 생성 (Bean아님)
//    public OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
//        return new CustomAuthorizationRequestResolver(
//                clientRegistrationRepository,
//                "/oauth2/authorization",
//                cryptoUtils  // 암호화 유틸 주입
//        );
//    }
//}
