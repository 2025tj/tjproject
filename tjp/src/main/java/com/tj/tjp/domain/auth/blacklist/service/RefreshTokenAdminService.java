package com.tj.tjp.domain.auth.blacklist.service;

import com.tj.tjp.domain.auth.blacklist.dto.RefreshTokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenAdminService {
    private final RedisTemplate<String, String> redisTemplate;

    // NOTE: RefreshTokenStoreService의 prefix와 반드시 동일하게 유지
    private static final String KEY_PREFIX = "rt:user:";

    /**
     * rt:user:* 키들을 SCAN으로 탐색해서 최대 Limit개 반환
     */
    public List<RefreshTokenInfo> listTokens(String pattern, int limit, int scanCount) {
        final String match = (pattern == null || pattern.isBlank()) ? KEY_PREFIX + "*" : pattern;
        final int max = (limit > 0) ? limit : 100;
        final int count = (scanCount > 0) ? scanCount : 1000;

        // SCAN으로 키 수집( 오버로드 모호성 방지: RedisCallback<List<String>> 캐스팅)
        List<String> keys = redisTemplate.execute(
                (RedisCallback<List<String>>) connection -> {
                    StringRedisSerializer s = new StringRedisSerializer();
                    ScanOptions options = ScanOptions.scanOptions().match(match).count(count).build();
                    List<String> out = new ArrayList<>();
                    try (Cursor<byte[]> cur = connection.scan(options)) {
                        while (cur.hasNext() && out.size() < max) {
                            String k = s.deserialize(cur.next());
                            if (k != null) out.add(k);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Redis SCAN 실패", e);
                    }
                    return out;
                }
        );
        if (keys == null) keys = List.of();

        // 키 상세 조회 (값 /ttl)
        List<RefreshTokenInfo> out = new ArrayList<>(keys.size());
        for (String key : keys) {
            String hash = redisTemplate.opsForValue().get(key);
            Long ttlMs = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS); // -1: no expire, -2: no key
            String email = key.startsWith(KEY_PREFIX) ? key.substring(KEY_PREFIX.length()) : key;
            out.add(new RefreshTokenInfo(email, key, preview(hash), ttlMs));
        }
        return out;
    }

    public boolean deleteByEmail(String email) {
        String key = KEY_PREFIX + email;
        Boolean r = redisTemplate.delete(key);
        return r != null && r;
    }

    public int deleteByPattern(String pattern, int scanCount) {
        final String match = (pattern == null || pattern.isBlank()) ? KEY_PREFIX + "*" : pattern;
        final int count = (scanCount > 0) ? scanCount : 1000;

        // SCAN으로 전체 키  수집
        List<String> keys = redisTemplate.execute(
                (RedisCallback<List<String>>) connection -> {
                    StringRedisSerializer s = new StringRedisSerializer();
                    ScanOptions options = ScanOptions.scanOptions().match(match).count(count).build();
                    List<String> out = new ArrayList<>();
                    try (Cursor<byte[]> cur = connection.scan(options)) {
                        cur.forEachRemaining(b -> {
                            String k = s.deserialize(b);
                            if (k != null) out.add(k);
                        });
                    } catch (Exception e) {
                        throw new RuntimeException("Redis SCAN 실패", e);
                    }
                    return out;
                }
        );
        if (keys == null || keys.isEmpty()) return 0;

        long deleted = redisTemplate.delete(keys);
        return Math.toIntExact(deleted);
    }

    private String preview(String hash) {
        if (hash == null) return null;
        int n = Math.min(12, hash.length());
        return hash.substring(0, n) + (hash.length() > n ? "..." : "");
    }
}
