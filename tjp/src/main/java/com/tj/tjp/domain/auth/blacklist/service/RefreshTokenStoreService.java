package com.tj.tjp.domain.auth.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenStoreService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "rt:user:"; // 사용자 단일 RT

    public void save(String email, String token, long ttlMillis) {
        String key = KEY_PREFIX + email;
        String hash = sha256Base64(token);
        redisTemplate.opsForValue().set(key, hash, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean matches(String email, String token) {
        String key = KEY_PREFIX + email;
        String saved = redisTemplate.opsForValue().get(key);
        if (saved == null) return false;
        return saved.equals(sha256Base64(token));
    }

    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }

    private static String sha256Base64(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(d);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
}
