package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.Payment;
import com.tj.tjp.domain.subscription.entity.PaymentStatus;
import com.tj.tjp.domain.subscription.entity.Subscription;
import com.tj.tjp.domain.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    // 구독 요약
    private Long subscriptionId;
    private String planName;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 결제 요약
    private Long paymentId;
    private String impUid;
    private String merchantUid;
    private Integer amount;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;

    public static PaymentResponse from(Subscription sub, Payment payment) {
        return PaymentResponse.builder()
                .subscriptionId(sub.getId())
                .planName(sub.getPlan().getName().name())
                .subscriptionStatus(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .paymentId(payment.getId())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
