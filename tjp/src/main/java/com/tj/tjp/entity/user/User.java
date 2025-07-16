package com.tj.tjp.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name="password", nullable = true)
    private String password;

    @Column(name="nickname", nullable=true, unique=true, length=30)
    private String nickname;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "user_id")  // FK 컬럼명 명시
    )
    @Column(name = "role")  // 컬럼명 명시
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name="email_verified_at", nullable = true)
    private LocalDateTime emailVerifiedAt;

    @Column(name="status", nullable = false, length = 20)
    @Builder.Default
    private String status = UserStatus.ACTIVE.name(); // ACTIVE, INACTIVE, BLOCKED 등

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /** 사용자 활성화 처리 */
    public void activate() {
        this.status = UserStatus.ACTIVE.name();
    }

    /** 사용자 비활성화 처리 (예: 탈퇴) */
    public void deactivate() {
        this.status = UserStatus.INACTIVE.name();
    }

    /** 사용자 차단 처리 */
    public void block() {
        this.status = UserStatus.BLOCKED.name();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
