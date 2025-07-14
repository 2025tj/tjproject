package com.tj.tjp.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix="app.jwt")
public class JwtConfig {
    /** JWT 서명에 사용할 비밀 키 */
    private String secret;
    /** AccessToken 만료 기간 (ms) */
    private Duration accessTokenTtl;
    /** RefreshToken 만료 기간 (ms) */
    private Duration refreshTokenTtl;
    /** 토큰 발급자(issuer) */
    private String issuer;
    /** jwt 꺼내는 위치 **/
    private String strategy;
}
