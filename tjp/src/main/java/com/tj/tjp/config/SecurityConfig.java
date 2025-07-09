package com.tj.tjp.config;

import com.tj.tjp.security.JwtAuthenticationFilter;
import com.tj.tjp.security.OAuth2SuccessHandler;
import com.tj.tjp.security.OAuth2UserCumstomService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final OAuth2UserCumstomService oAuth2UserCumstomService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // csrf 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/me").authenticated() // 토큰 필요하게 설정
                        .requestMatchers("/api/users/**", "/login/**").permitAll() // 회원가입, 로그인은 인증없이 허용
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserCumstomService)
                        )
                        .successHandler(oAuth2SuccessHandler)
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
}
