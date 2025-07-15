package com.tj.tjp.repository.user;

import com.tj.tjp.entity.user.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
