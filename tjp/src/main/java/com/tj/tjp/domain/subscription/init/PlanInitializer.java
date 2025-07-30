package com.tj.tjp.domain.subscription.init;

import com.tj.tjp.domain.subscription.entity.Plan;
import com.tj.tjp.domain.subscription.entity.PlanType;
import com.tj.tjp.domain.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class PlanInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {
        for (PlanType planType : EnumSet.allOf(PlanType.class)) {
            planRepository.findByName(planType).orElseGet(() -> {
                Plan plan = Plan.builder()
                        .name(planType)
                        .price(getPrice(planType))
                        .description(getDescription(planType))
                        .build();
                return planRepository.save(plan);
            });
        }
    }

    private int getPrice(PlanType type) {
        return switch (type) {
            case TRIAL -> 0;
            case MONTHLY -> 9900;
            case YEARLY -> 99000;
        };
    }

    private String getDescription(PlanType planType) {
        return switch (planType) {
            case TRIAL -> "7일 무료 체험";
            case MONTHLY -> "월간 구독";
            case YEARLY -> "연간 구독 (2개월 할인)";
        };
    }
}
