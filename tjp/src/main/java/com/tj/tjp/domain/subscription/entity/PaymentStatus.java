package com.tj.tjp.domain.subscription.entity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum PaymentStatus {
    READY,  // 결제 시도 전
    PAID,   // 결제 성공
    FAILED, // 결제 실패
    CANCELLED;  // 결제 취소/환불

    public static PaymentStatus from(String status) {
        if (status == null) {
            return READY;
        }
        switch (status.toLowerCase()) {
            case "paid" -> {
                return PAID;
            }
            case "cancelled", "canceled" -> {
                return CANCELLED;
            }
            case "failed" -> {
                return FAILED;
            }
            default -> {
                log.warn("알 수 없는 결제 상태: {} -> READY로 처리", status);
                return READY;
            }
        }
    }
}
