package com.tj.tjp.domain.email.entity;

import com.tj.tjp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증 토큰 엔티티
 * - 회원가입 등에서 이메일 인증을 위한 1회성 토큰을 관리
 * - 토큰은 만료시간, 사용 여부를 함께 저장
 */
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="email_verification_tokens")
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 인증 토큰 (UUID 등 랜덤 문자열)
     */
    private String token; // UUID 등

    /**
     * 토큰이 소속된 사용자 (ManyToOne 연관관계, FK: user_id)
     */
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name= "user_id", nullable = false)
    private User user; // FK or User user;

    /**
     * 토큰 만료 시각 (24시간 등)
     */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /**
     * 사용 여부 (1회용: true면 이미 사용된 토큰)
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean used = false; // 1회성

    /**
     * 토큰 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 사용 처리 (used → true)
     */
    public void markUsed(boolean used) {
        this.used = used;
    }

    /**
     * 만료 시각 재설정 (재발급 등 필요시)
     * @param expiredAt 새 만료 시각
     */
    public void extendExpiry(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
