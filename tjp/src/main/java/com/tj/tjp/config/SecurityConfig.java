package com.tj.tjp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // csrf 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").permitAll() // 회원가입, 로그인은 인증없이 허용
                        .anyRequest().authenticated()
                )
//                .formLogin(Customizer.withDefaults()); // 기본인증방식
                .httpBasic(Customizer.withDefaults()); // 기본인증방식
        return http.build();
    }
}
