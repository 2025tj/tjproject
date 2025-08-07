package com.tj.tjp.domain.auth.blacklist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
        return redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + token);
    }
}
