package com.tj.tjp.domain.subscription.dto;

import com.tj.tjp.domain.subscription.entity.PlanType;
import com.tj.tjp.domain.subscription.entity.RefundRequest;
import com.tj.tjp.domain.subscription.entity.RefundStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RefundRequestDto(
    Long id,
    String email,
    PlanType plantype,
    Integer originalPrice,
    Integer approvedAmount,
    LocalDateTime requestedAt,
    RefundStatus status
) {
    public static RefundRequestDto from(RefundRequest request) {
        return RefundRequestDto.builder()
                .id(request.getId())
                .email(request.getUser().getEmail())
                .plantype(request.getSubscription().getPlan().getName())
                .originalPrice(request.getSubscription().getPlan().getPrice())
                .approvedAmount(request.getApprovedAmount())
                .requestedAt(request.getRequestedAt())
                .status(request.getStatus())
                .build();
    }
}
