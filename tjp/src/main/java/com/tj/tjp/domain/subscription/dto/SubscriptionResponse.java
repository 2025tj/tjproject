package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.PlanType;
import com.tj.tjp.domain.subscription.entity.Subscription;
import com.tj.tjp.domain.subscription.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponse {

    @Schema(description = "요금제 정보")
    private PlanDto plan;
    @Schema(description = "구독 시작일", example = "2025-07-30T00:00:00")
    private LocalDateTime startDate;
    @Schema(description = "구독 종료일", example = "2025-08-30T00:00:00")
    private LocalDateTime endDate;
    @Schema(description = "활성 구독 여부", example = "true")
    private boolean isActive;
    @Schema(description = "구독 상태", example = "ACTIVE")
    private SubscriptionStatus status;

    public static SubscriptionResponse from(Subscription subscription) {
        return SubscriptionResponse.builder()
                .plan(PlanDto.from(subscription.getPlan()))
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .isActive(subscription.isActive())
                .status(subscription.getStatus())
                .build();
    }
}
