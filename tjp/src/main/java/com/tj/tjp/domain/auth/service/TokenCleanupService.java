package com.tj.tjp.domain.auth.service;

import com.tj.tjp.domain.email.repository.EmailVerificationTokenRepository;
import com.tj.tjp.domain.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * 만료된 이메일 인증 토큰 정리
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredEmailVerificationTokens() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 7일 전 토큰까지 정리

            // 만료된 토큰 조회 및 삭제
            int deletedCount = emailVerificationTokenRepository.deleteByExpiredAtBefore(cutoffTime);

            log.info("만료된 이메일 인증 토큰 정리 완료: {} 개 삭제", deletedCount);

        } catch (Exception e) {
            log.error("만료된 이메일 인증 토큰 정리 실패", e);
        }
    }

    /**
     * 만료된 비밀번호 재설정 토큰 정리
     * 매일 새벽 2시 10분에 실행
     */
    @Scheduled(cron = "0 10 2 * * *")
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1); // 1일 전 토큰까지 정리

            // 만료된 토큰 조회 및 삭제
            int deletedCount = passwordResetTokenRepository.deleteByExpiredAtBefore(cutoffTime);

            log.info("만료된 비밀번호 재설정 토큰 정리 완료: {} 개 삭제", deletedCount);

        } catch (Exception e) {
            log.error("만료된 비밀번호 재설정 토큰 정리 실패", e);
        }
    }

    /**
     * 사용된 토큰 정리 (선택적)
     * 매주 일요일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupUsedTokens() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30); // 30일 전 사용된 토큰까지 정리

            // 사용된 이메일 인증 토큰 삭제
            int deletedEmailTokens = emailVerificationTokenRepository.deleteByUsedTrueAndCreatedAtBefore(cutoffTime);

            // 사용된 비밀번호 재설정 토큰 삭제
            int deletedPasswordTokens = passwordResetTokenRepository.deleteByUsedTrueAndCreatedAtBefore(cutoffTime);

            log.info("사용된 토큰 정리 완료: 이메일 인증 토큰 {} 개, 비밀번호 재설정 토큰 {} 개 삭제",
                    deletedEmailTokens, deletedPasswordTokens);

        } catch (Exception e) {
            log.error("사용된 토큰 정리 실패", e);
        }
    }
}
