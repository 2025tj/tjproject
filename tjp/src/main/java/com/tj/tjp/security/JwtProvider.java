package com.tj.tjp.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private final long EXPIRATION_MS=1000*60*60;

    public String createToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String validateAndGetEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
