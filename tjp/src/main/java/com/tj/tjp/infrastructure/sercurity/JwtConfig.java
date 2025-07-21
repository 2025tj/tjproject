package com.tj.tjp.infrastructure.sercurity;

import com.tj.tjp.infrastructure.config.properties.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

//    private final JwtProvider jwtProvider;
//    private final TokenService tokenService;
    private final JwtProperties jwtProperties;  // JWT 설정값


//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() {
//        return new JwtAuthenticationFilter(jwtProvider, tokenService);
//    }
}
