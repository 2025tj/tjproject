package com.tj.tjp.domain.auth.repository;

import com.tj.tjp.domain.auth.entity.PasswordResetToken;
import com.tj.tjp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * 토큰으로 비밀번호 재설정 토큰 조회
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 사용자의 미사용 토큰 목록 조회
     */
    List<PasswordResetToken> findByUserAndUsedFalse(User user);

    /**
     * 사용자의 모든 토큰 조회
     */
    List<PasswordResetToken> findByUser(User user);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiredAt < :cutoffTime")
    int deleteByExpiredAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 사용된 토큰 중 생성일이 특정 시점 이전인 것들 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.used = true AND p.createdAt < :cutoffTime")
    int deleteByUsedTrueAndCreatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
}
