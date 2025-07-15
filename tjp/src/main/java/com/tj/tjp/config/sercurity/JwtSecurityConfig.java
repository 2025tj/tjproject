package com.tj.tjp.config.sercurity;

import com.tj.tjp.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {

    // JWT 인증 처리를 담당하는 커스텀 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * HTTP 시큐리티 설정을 구성합니다.
     */
    public void configureJwt(HttpSecurity http) throws Exception {
        http
            // 1. 엔드포인트별 권한 설정
            .authorizeHttpRequests(auth -> auth
                    //    - "/api/users/me" 경로는 인증된 사용자만 접근 허용
                    .requestMatchers("/api/users/me", "/api/users/pending-social-link").authenticated()
                    //    - "/api/auth/**" 및 "/api/users/**" 경로는 모두 허용 (회원가입·로그인 등)
                    .requestMatchers(
                            "/api/auth/**",
                            "/api/auth/signup",
                            "/api/auth/verify"
                    ).permitAll()
                    //    - 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            )
            // 2. 세션 관리 설정: 세션을 사용하지 않고 JWT 기반 무상태(stateless)로 처리
            .sessionManagement(sm -> sm
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 3. JWT 인증 필터 등록
            //    - UsernamePasswordAuthenticationFilter 전에 실행되어
            //      요청 헤더(또는 쿠키)에서 토큰을 추출하고 검증함
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
    }
}
