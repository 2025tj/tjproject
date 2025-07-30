package com.tj.tjp.domain.subscription.controller;

import com.tj.tjp.domain.subscription.dto.SubscriptionRequest;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.service.SubscriptionService;
import com.tj.tjp.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Subscription", description = "구독 관련 API")
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 구독 생성
    @PostMapping
    @Operation(
            summary = "구독 생성",
            description = "요금제를 선택하여 유저의 구독 생성")
    public ResponseEntity<?> subscribe(
            @AuthenticationPrincipal User user,
            @RequestBody SubscriptionRequest request
            ) {
        log.info("@@@ createSubscription 진입 @@@");
        subscriptionService.createSubscription(user, request.getPlan());
        log.info("@@@ createSubscription 진행됨 @@@");
        return ResponseEntity.ok("구독이 성공적으로 생성되었습니다.");
    }

    // 구독 상태 확인
    @GetMapping("/activate")
    @Operation(
            summary = "구독 상태 확인",
            description = "현재 로그인한 유저의 유효한 구독이 있는지 확인")
    public ResponseEntity<Boolean> isSubscribed(@AuthenticationPrincipal User user) {
        boolean active = subscriptionService.hasValidSubscription(user);
        return ResponseEntity.ok(active);
    }

    @GetMapping("/me")
    @Operation(summary = "구독 상세 정보 조회", description = "현재 로그인한 유저의 구독정보를 반환")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class)))
    public ResponseEntity<SubscriptionResponse> getMySubscription(@AuthenticationPrincipal User user) {
        SubscriptionResponse response = subscriptionService.getSubscriptionDetails(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
            summary = "구독 즉시 해지",
            description = "현재 사용자의 활성 구독을 즉시 해지합니다"
    )
    public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal User user) {
        subscriptionService.unsubscribe(user);
        return ResponseEntity.ok("구독이 즉시 해지되었습니다.");
    }

    @PatchMapping("/cancel")
    @Operation(summary = "구독 해지 예약", description = "현재 구독을 해지 요청. 기간 만료 후 자동 만료")
    public ResponseEntity<String> cancelSubscription(@AuthenticationPrincipal User user) {
        subscriptionService.cancelSubscription(user);
        return ResponseEntity.ok("구독 해지 요청 완료. 만료일까지는 사용 가능합니다.");
    }
}
