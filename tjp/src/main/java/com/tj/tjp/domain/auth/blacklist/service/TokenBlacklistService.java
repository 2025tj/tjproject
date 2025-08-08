package com.tj.tjp.domain.auth.blacklist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "bl:refresh";

    // 블랙리스트 등록
    public void blacklistRefreshToken(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + token,
                "logout",
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        boolean exists = redisTemplate.hasKey(key);
        log.info("블랙리스트 확인: key={}, 존재={}", key, exists);
        return exists;
    }
}
