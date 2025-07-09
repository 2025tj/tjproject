package com.tj.tjp.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogTestRunner {

    @PostConstruct
    public void init() {
        log.info("Log4j2 INFO 로그입니다.");
        log.debug("Log4j2 DEBUG 로그입니다.");
        log.warn("Log4j2 WARN 로그입니다.");
        log.error("Log4j2 ERROR 로그입니다.");
    }
}
