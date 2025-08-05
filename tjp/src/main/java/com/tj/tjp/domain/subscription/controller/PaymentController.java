package com.tj.tjp.domain.subscription.controller;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import com.tj.tjp.domain.subscription.dto.PaymentCompleteRequest;
import com.tj.tjp.domain.subscription.dto.PaymentResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
import com.tj.tjp.domain.subscription.service.PaymentService;
import com.tj.tjp.domain.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(
            @AuthenticationPrincipal LocalUserPrincipal principal,
            @RequestBody PaymentCompleteRequest request
            ) {
        PaymentResponse response = paymentService.completePayment(principal.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestParam String impUid,
                                           @RequestParam(required = false) Integer amount) {
        try {
            PaymentResponse response = paymentService.refundPayment(impUid, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("환불 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("환불 실패: " + e.getMessage());
        }
    }

//    @DeleteMapping
//    @Operation(
//            summary = "구독 즉시 해지 (환불 포함)",
//            description = "현재 사용자의 활성 구독을 즉시 해지하고, 결제가 존재하면 환불을 진행합니다."
//    )
//    public ResponseEntity<?> unsubscribeAndRefund(
//            @AuthenticationPrincipal LocalUserPrincipal principal,
//            @RequestParam(required = false) Integer amount) {
//        try {
//            PaymentResponse response = paymentService.unsubscribeAndRefund(principal.getUser(), amount);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("구독 해지/환불 실패", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("구독 해지 실패: " + e.getMessage());
//        }
//    }
    @DeleteMapping
    @Operation(
            summary = "구독 즉시 해지 (환불 포함)",
            description = "현재 사용자의 활성 구독을 즉시 해지하고, 결제가 존재하면 환불을 진행합니다."
    )
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> unsubscribeAndRefund(
            @AuthenticationPrincipal LocalUserPrincipal principal,
            @RequestParam(required = false) Integer amount) {
        try {
            SubscriptionStatusResponse response = subscriptionService.unsubscribeAndRefund(principal.getUser(), amount);
            return ResponseEntity.ok(ApiResponse.<SubscriptionStatusResponse>success("구독이 해지 및 환불되었습니다.", response));
        } catch (Exception e) {
            log.error("구독 해지/환불 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SubscriptionStatusResponse>error("구독 해지 실패: " + e.getMessage()));
        }
    }
}
