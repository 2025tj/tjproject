package com.tj.tjp.domain.auth.security.jwt;

import com.tj.tjp.infrastructure.config.properties.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
/**
 * JWT 토큰의 생성, 검증 및 클레임 추출을 담당하는 컴포넌트
 */
@Component
public class JwtProvider {

    // application.yml의 app.jwt 설정을 바인딩한 설정 클래스
    private final JwtProperties jwtProperties;

    private Key secretKey;

    /**
     * JwtConfig를 주입받는 생성자에 @Autowired를 붙이면,
     * Spring이 이 생성자를 통해 bean을 생성합니다.
     */
    @Autowired
    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 생성된 후에 secretKey를 초기화합니다.
     */
//    @PostConstruct
//    public void init() {
//        this.secretKey = Keys.hmacShaKeyFor(
//                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
//        );
//    }
    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        log.info("Loaded JWT secret from properties: [{}]", secret);
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            log.info("Decoded secret length: {}", keyBytes.length);
        } catch (Exception e) {
            log.error("Failed to decode JWT secret from base64", e);
            throw e; // 애플리케이션 시작 실패 유도
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
//    @PostConstruct
//    public void init() {
//        byte[] keyBytes = Base64.getUrlDecoder().decode(jwtProperties.getSecret());
//        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
//    }


    /**
     * AccessToken 생성 (subject: email, claim: roles)
     */
    public String createAccessToken(String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles.toArray(new String[0]))
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtProperties.getAccessTokenTtl().toMillis())))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * RefreshToken 생성 (subject: email)
     */
    public String createRefreshToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtProperties.getRefreshTokenTtl().toMillis())))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰 (Access, Refresh) 유효성 검증
     * 만료, 변조 여부를 검사하여 예외 발생 시 false 반환
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * RefreshToken 전용 검증 (필요 시 로직 분리)
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    /**
     * 토큰에서 subject(email) 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 토큰에서 roles 클레임 추출
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String[] rolesArray = claims.get("roles", String[].class);
        return (rolesArray != null)
                ? Arrays.asList(rolesArray)
                : List.of();
    }

    public long getRefreshTokenRemainingMillis(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            return expiration.getTime() - now;
        } catch (Exception e) {
            log.warn("만료 시간 추출 실패: {}", e.getMessage());
            return 0;
        }
    }


}
