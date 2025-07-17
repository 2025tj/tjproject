package com.tj.tjp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class OneTimeTokenProvider {

    @Value("${app.one-time-jwt.secret}")
    private String secret;

    /** 분 단위로 설정된 짧은 만료 시간 (예: 10) */
    @Value("${app.one-time-jwt.expiry-minutes}")
    private long expiryMinutes;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * email, provider 정보를 담아 일회용 링크용 JWT를 생성합니다.
     * TTL은 application.yml의 jwt.oneTime.expiryMinutes에 따릅니다.
     */
    public String createToken(String email, String provider, String providerId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("oauth2-link")
                .claim("email",    email)
                .claim("provider", provider)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiryMinutes * 60)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 전달받은 토큰의 서명 및 만료를 검증하고,
     * 문제가 없으면 내부 Claims를 반환합니다.
     * 오류 시 JwtException을 던집니다.
     */
    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    public String extractProvider(String token) {
        Claims claims = parseClaims(token);
        return claims.get("provider", String.class);
    }

    /**
     * 토큰에서 프로바이더 ID 추출
     */
    public String extractProviderId(String token) {
        return extractClaims(token).get("providerId", String.class);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("일회용 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}