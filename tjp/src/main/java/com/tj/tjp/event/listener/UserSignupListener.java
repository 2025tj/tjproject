package com.tj.tjp.event.listener;

import com.tj.tjp.event.UserSignupEvent;
import com.tj.tjp.service.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupListener {
    private final EmailVerificationService emailVerificationService;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        try {
            emailVerificationService.sendVerificationEmail(event.getUser());
        } catch (Exception e) {
            log.error("회원가입 이메일 인증 발송 실패", e);
        }
    }
}
