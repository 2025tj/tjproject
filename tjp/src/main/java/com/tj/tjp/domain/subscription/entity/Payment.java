package com.tj.tjp.domain.subscription.entity;

import com.tj.tjp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 연결된 구독(선결제 테스트시 없앰)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription  subscription;

    //아임 포트 고유 ID
    @Column(nullable = false, unique = true)
    private String impUid;

    // 내 시스템 주문 번호
    @Column(nullable = false, unique = true)
    private String merchantUid;

    // 결제 금액
    @Column(nullable = false)
    private Integer amount;

    // 결제 상태(PAID, CANCELLED, FAILED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 결제 일시
    private LocalDateTime paidAt;

    // 취소/환불 일시
    private LocalDateTime cancelledAt;

    // ---상태변경메서드---
    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        this.cancelledAt = null;
    }

    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus == null) return;

        if (this.status == newStatus) {
            return;
        }
        this.status = newStatus;

        switch (newStatus) {
            case PAID -> {
                if (this.paidAt == null) {
                    this.paidAt = LocalDateTime.now();
                }
            }
            case CANCELLED -> {
                if (this.cancelledAt == null) {
                    this.cancelledAt = LocalDateTime.now();
                }
            }
        }
    }

    public static Payment of(User user, Subscription subscription,
                             String impUid, String merchantUid,
                             Integer amount, PaymentStatus status) {
        return Payment.builder()
                .user(user)
                .subscription(subscription)
                .impUid(impUid)
                .merchantUid(merchantUid)
                .amount(amount)
                .status(status)
                .paidAt(status == PaymentStatus.PAID ? LocalDateTime.now() : null)
                .build();
    }
}
