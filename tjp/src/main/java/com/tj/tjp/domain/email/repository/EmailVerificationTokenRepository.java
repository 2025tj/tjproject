package com.tj.tjp.domain.email.repository;

import com.tj.tjp.domain.email.entity.EmailVerificationToken;
import com.tj.tjp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이메일 인증 토큰 JPA 리포지토리
 * - 인증 메일 발송/검증 등에 사용되는 토큰 데이터에 대한 DB 접근을 담당
 * - findByToken()으로 토큰 값 기반 인증 처리
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    /**
     * 인증 토큰 값으로 토큰 엔티티 조회
     * @param token 이메일 인증용 토큰(랜덤 UUID 등)
     * @return Optional<EmailVerificationToken>
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * 특정 사용자의 미사용 토큰들 조회 (재발송 시 기존 토큰 무효화용)
     * @param user 사용자 엔티티
     * @return 미사용 토큰 리스트
     */
    List<EmailVerificationToken> findByUserAndUsedFalse(User user);

    /**
     * 특정 사용자의 모든 토큰 조회
     * @param user 사용자 엔티티
     * @return 토큰 리스트
     */
    List<EmailVerificationToken> findByUser(User user);

    /**
     * 만료된 토큰들 조회 (배치 정리용)
     * @param now 현재 시간
     * @return 만료된 토큰 리스트
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.expiredAt < :now")
    List<EmailVerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 특정 사용자의 유효한 토큰 개수 조회 (재발송 제한용)
     * @param user 사용자 엔티티
     * @param now 현재 시간
     * @return 유효한 토큰 개수
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user = :user AND t.used = false AND t.expiredAt > :now")
    long countValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiredAt < :cutoffTime")
    int deleteByExpiredAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 사용된 토큰 중 생성일이 특정 시점 이전인 것들 삭제
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.used = true AND e.createdAt < :cutoffTime")
    int deleteByUsedTrueAndCreatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
}
