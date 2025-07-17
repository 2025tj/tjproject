package com.tj.tjp.config.sercurity;

import com.tj.tjp.config.properties.jwt.JwtProperties;
import com.tj.tjp.security.filter.JwtAuthenticationFilter;
import com.tj.tjp.security.jwt.JwtProvider;
import com.tj.tjp.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
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
