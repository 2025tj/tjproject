package com.tj.tjp.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private final long EXPIRATION_MS=1000*60*60;

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String createToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndGetEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
