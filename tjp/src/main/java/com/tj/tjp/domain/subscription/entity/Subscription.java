package com.tj.tjp.domain.subscription.entity;

import com.tj.tjp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저의 구독인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 구독 플랜
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    // 구독 활성화 로직
    public void activate(LocalDateTime start, LocalDateTime end) {
        this.startDate = start;
        this.endDate = end;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // 현재 유효한 구독인지
    public boolean isValid() {
        return this.status == SubscriptionStatus.ACTIVE && endDate.isAfter(LocalDateTime.now());
    }

    public static Subscription of(User user, Plan plan, LocalDateTime start, LocalDateTime end) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(start)
                .endDate(end)
                .isActive(false)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        subscription.activate(start, end);
        return subscription;
    }

    public void cancel() {
        if (this.status == SubscriptionStatus.ACTIVE) {
            this.status = SubscriptionStatus.CANCELLED;
        }
    }

    public void revertCancel() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            this.status = SubscriptionStatus.ACTIVE;
            this.isActive = true;
        } else {
            throw new IllegalArgumentException("해지 예약 상태가 아닌 구독은 복원할 수 없습니다.");
        }
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.isActive = false;
    }
}
