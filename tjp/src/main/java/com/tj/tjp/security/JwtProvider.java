package com.tj.tjp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private final long ACCESS_TOKEN_EXPIRATION_MS=1000*60*15; // 15분
    private final long REFRESH_TOKEN_EXPIRATION_MS = 1000L*60*60*24*7; // 7일

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String createAccessToken(String email, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();

//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + Duration.ofHours(1).toMillis());

//        return Jwts.builder()
//                .setSubject(email)
//                .claim("roles", roles)
//                .setIssuedAt(now)
//                .setExpiration(expiry)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", List.class);
    }

    public String createRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration((new Date(System.currentTimeMillis() +
                        REFRESH_TOKEN_EXPIRATION_MS)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 기본 accessToken 발급용 (단일 ROLE_USER 부여)
    public String createToken(String email) {
        return createAccessToken(email, List.of("ROLE_USER"));
    }

    public String getEmailFromToken(String token) {
        log.info("jwtprovider 전달받은토큰: {}", token);
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
