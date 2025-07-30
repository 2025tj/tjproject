package com.tj.tjp.domain.subscription.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum PlanType {
    TRIAL(30),
    MONTHLY(30),
    YEARLY(365);

    private final int durationDays;

    // 역직렬화: "monthly" -> PlanType.MONTHLY
    @JsonCreator
    public static PlanType from(String value) {
        log.info("PlanType 역직렬화 시도: {}", value);
        try {
            PlanType planUpper = PlanType.valueOf(value.toUpperCase());
            log.info("역직렬화된 값 {}", planUpper);
            return planUpper;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("지원하지 않는 요금제입니다: " + value);
        }
    }

    // 직렬화: PlanType.MONTHLY -> "monthly"
    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }
}
