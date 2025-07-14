package com.tj.tjp.security.jwt;

import com.tj.tjp.config.jwt.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
/**
 * JWT 토큰의 생성, 검증 및 클레임 추출을 담당하는 컴포넌트
 */
@Component
public class JwtProvider {

    // application.yml의 app.jwt 설정을 바인딩한 설정 클래스
    private final JwtConfig jwtConfig;

    private Key secretKey;

    /**
     * JwtConfig를 주입받는 생성자에 @Autowired를 붙이면,
     * Spring이 이 생성자를 통해 bean을 생성합니다.
     */
    @Autowired
    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 생성된 후에 secretKey를 초기화합니다.
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * AccessToken 생성 (subject: email, claim: roles)
     */
    public String createAccessToken(String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles.toArray(new String[0]))
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtConfig.getAccessTokenTtl().toMillis())))
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
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtConfig.getRefreshTokenTtl().toMillis())))
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
}
