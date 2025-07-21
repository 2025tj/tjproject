package com.tj.tjp.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 사용자 상태를 표현하는 열거형
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED;

    @JsonCreator
    public static UserStatus from(String value) {
        try {
            return UserStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 상태: " + value);
        }
    }

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }
}
