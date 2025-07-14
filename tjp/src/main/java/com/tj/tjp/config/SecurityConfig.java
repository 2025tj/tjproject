package com.tj.tjp.config;

//import com.tj.tjp.security.CustomOAuth2FailureHandler;
import com.tj.tjp.security.CustomOAuth2UserService;
import com.tj.tjp.security.JwtAuthenticationFilter;
import com.tj.tjp.security.OAuth2SuccessHandler;
//import com.tj.tjp.security.OAuth2UserCumstomService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable()) // csrf 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/me").authenticated() // 토큰 필요하게 설정
                        .requestMatchers("/api/users/**",
                                "/api/auth/**",
                                "/api/auth/signup",
                                "/api/auth/signup/local",
                                "/api/auth/login",
                                "/api/auth/login/local",
                                "/api/auth/refresh",
                                "/login/**",
                                "/oauth2/**").permitAll() // 회원가입, 로그인은 인증없이 허용
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redir -> redir
                                .baseUri("/oauth2/callback/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler) // 토큰발급후 리디렉션
//                        .failureHandler(customOAuth2FailureHandler)
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy
                                .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none';")
                        )
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//                .formLogin(Customizer.withDefaults()); // 기본인증방식
//                .httpBasic(Customizer.withDefaults()); // 기본인증방식
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
