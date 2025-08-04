package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.PlanType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentCompleteRequest {
    private String impUid; // 아임포트 결제 고유 ID
    private String merchantUid; // 내 서버 주문번호
    private Integer amount; // 결제 금액
    private PlanType planType; // 어떤 요금제 결제인지
}
