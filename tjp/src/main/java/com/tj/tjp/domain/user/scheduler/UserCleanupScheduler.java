package com.tj.tjp.domain.user.scheduler;

import com.tj.tjp.domain.user.service.UserLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserLifecycleService userLifecycleService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void purgeWithdrawnUsers() {
        int deleted = userLifecycleService.hardDeleteDueUsers();
        if (deleted > 0) {
            log.info("Hard-deleted {} users whose grace period ended.", deleted);
        }
    }
}
