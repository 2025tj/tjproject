package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.Plan;
import com.tj.tjp.domain.subscription.entity.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanDto {

    @Schema(description = "요금제 이름", example = "monthly")
    private PlanType name;
    @Schema(description = "요금제 가격", example = "10000")
    private int price;
    @Schema(description = "요금제 설명", example ="월간 구독 요금제입니다.")
    private String description;

    public static PlanDto from(Plan plan) {
        return PlanDto.builder()
                .name(plan.getName())
                .price(plan.getPrice())
                .description(plan.getDescription())
                .build();
    }
}
