package com.tj.tjp.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProviderType {
    LOCAL,
    GOOGLE;

    @JsonCreator
    public static ProviderType from(String provider) {
        try {
            return ProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("지원하지 않는 소셜로그인 제공자:" + provider);
        }
    }

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase(); // 직렬화시 소문자로 보임, 프론트에서 소문자 사용가능해짐
    }
}
