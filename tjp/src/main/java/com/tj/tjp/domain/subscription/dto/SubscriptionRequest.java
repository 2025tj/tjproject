package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SubscriptionRequest {

    @Schema(
            description = "요금제 종류(trial, monthly, yearly 중 하나",
            example = "monthly",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PlanType plan;
}
