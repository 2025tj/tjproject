package com.tj.tjp.domain.subscription.entity;

public enum PaymentStatus {
    READY,  // 결제 시도 전
    PAID,   // 결제 성공
    FAILED, // 결제 실패
    CANCELLED,  // 결제 취소/환불
}
