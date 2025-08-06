package com.tj.tjp.domain.subscription.controller;

import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import com.tj.tjp.domain.subscription.dto.PaymentResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionRequest;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
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
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @AuthenticationPrincipal LocalUserPrincipal principal,
            @RequestBody SubscriptionRequest request
            ) {
        log.info("@@@ createSubscription 진입 @@@");
        subscriptionService.createSubscription(principal.getUser(), request.getPlan());
        log.info("@@@ createSubscription 진행됨 @@@");
        return ResponseEntity.ok("구독이 성공적으로 생성되었습니다.");
    }

    // 구독 상태 확인
    @GetMapping("/activate")
    @Operation(
            summary = "구독 상태 확인",
            description = "현재 로그인한 유저의 유효한 구독이 있는지 확인")
    public ResponseEntity<Map<String, Boolean>> isSubscribed(@AuthenticationPrincipal LocalUserPrincipal principal) {
        boolean active = subscriptionService.hasValidSubscription(principal.getUser());
        return ResponseEntity.ok(Map.of("isActive", active));
    }

//    @GetMapping("/me")
//    @Operation(summary = "구독 상세 정보 조회", description = "현재 로그인한 유저의 구독정보를 반환")
//    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class)))
//    public ResponseEntity<SubscriptionResponse> getMySubscription(@AuthenticationPrincipal LocalUserPrincipal principal) {
//        log.info(">> [getMySubscription] user={}", principal.getUser());
//        SubscriptionResponse response = subscriptionService.getSubscriptionDetails(principal.getUser());
//        return ResponseEntity.ok(response);
//    }
    @GetMapping("/me")
    @Operation(summary = "구독 상세 정보 조회", description = "현재 로그인한 유저의 구독정보를 반환")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class)))
    public ResponseEntity<com.tj.tjp.common.dto.ApiResponse<SubscriptionStatusResponse>> getMySubscription(@AuthenticationPrincipal LocalUserPrincipal principal) {
        log.info(">> [getMySubscription] user={}", principal.getUser());
        SubscriptionStatusResponse response = subscriptionService.getSubscriptionStatus(principal.getUser());
        return ResponseEntity.ok(com.tj.tjp.common.dto.ApiResponse.<SubscriptionStatusResponse>success(response));
    }

    @DeleteMapping
    @Operation(
            summary = "구독 즉시 해지",
            description = "현재 사용자의 활성 구독을 즉시 해지합니다"
    )
    public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal LocalUserPrincipal principal) {
        subscriptionService.unsubscribe(principal.getUser());
        return ResponseEntity.ok("구독이 즉시 해지되었습니다.");
    }

    @PatchMapping("/cancel")
    @Operation(summary = "구독 해지 예약", description = "현재 구독을 해지 요청. 기간 만료 후 자동 만료")
    public ResponseEntity<String> cancelSubscription(@AuthenticationPrincipal LocalUserPrincipal principal) {
        subscriptionService.cancelSubscription(principal.getUser());
        return ResponseEntity.ok("구독 해지 요청 완료. 만료일까지는 사용 가능합니다.");
    }

    @PatchMapping("/cancel/revert")
    @Operation(summary = "구독 해지 예약 취소", description = "현재 구독 해지 예약 취소 요청.")
    public ResponseEntity<String> revertCancel(@AuthenticationPrincipal LocalUserPrincipal principal) {
        subscriptionService.revertCancel(principal.getUser());
        return ResponseEntity.ok("해지 예약이 취소되었습니다.");
    }

    @PostMapping("/refund-request")
    @Operation(summary = "환불 요청 생성")
    public ResponseEntity<com.tj.tjp.common.dto.ApiResponse<Void>> requestRefund(
            @AuthenticationPrincipal LocalUserPrincipal principal
    ) {
        subscriptionService.createRefundRequest(principal.getUser());
        return ResponseEntity.ok(com.tj.tjp.common.dto.ApiResponse.success("환불 요청이 접수되었습니다."));
    }
}
