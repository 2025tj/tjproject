package com.tj.tjp.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis-test")
@RequiredArgsConstructor
public class RedisTestController {
    private final RedisTestService redisTestService;
    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisTestService.setValue(key, value);
        return "Saved";
    }
    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        return redisTestService.getValue(key);
    }
}
