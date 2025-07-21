package com.tj.tjp.event.listener;

import com.tj.tjp.event.EmailVerificationResendEvent;
import com.tj.tjp.domain.email.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationResendListener {
    private final EmailVerificationService emailVerificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailVerificationResend(EmailVerificationResendEvent event) {
        try {
            emailVerificationService.sendVerificationEmail(event.getUser());
        } catch (Exception e) {
            log.error("이메일 인증 재발송 실패", e);
        }
    }
}
