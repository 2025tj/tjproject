package com.tj.tjp.repository.user;

import com.tj.tjp.entity.user.PasswordResetToken;
import com.tj.tjp.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
