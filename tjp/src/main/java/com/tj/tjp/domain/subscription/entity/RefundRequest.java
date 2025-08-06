package com.tj.tjp.domain.subscription.entity;

import com.tj.tjp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subscription subscription;

//    private Integer requestedAmount;
    private Integer approvedAmount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public void approve(Integer amount) {
        this.status = RefundStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = RefundStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
}
