package com.tj.tjp.domain.auth.dto.login;

import com.tj.tjp.domain.subscription.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
public class LoginResult {
    // 사용자 정보
    private final String email;
//    private final String message;
//    private final String warning; // null이면 경고 없음
    private final Set<String> roles;
    private final String nickname;
    private final boolean emailVerified;

    // 구독 상태 요약
    private final boolean subscribed;
    private final PlanType planType;
    private final LocalDateTime subEndDate;
}
