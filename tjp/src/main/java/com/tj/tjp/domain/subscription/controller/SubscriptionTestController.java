package com.tj.tjp.domain.subscription.controller;

import com.tj.tjp.domain.subscription.dto.SubscriptionRequest;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.service.SubscriptionService;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Subscription Test", description = "구독 테스트용 API (인증 불필요)")
@RestController
@RequestMapping("/api/subscription/test")
@RequiredArgsConstructor
public class SubscriptionTestController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    // 테스트용 구독 생성
    @PostMapping("/{userId}")
    @Operation(
            summary = "테스트용 구독 생성",
            description = "사용자 ID를 직접 지정하여 구독을 생성합니다. (인증 불필요)")
    public ResponseEntity<?> subscribeTest(
            @PathVariable Long userId,
            @RequestBody SubscriptionRequest request
    ) {
        log.info("@@@ 테스트용 구독 생성 진입 - UserId: {}, PlanType: {} @@@", userId, request.getPlan());
        log.info("@@@ UserRepository 객체: {} @@@", userRepository);
        log.info("@@@ SubscriptionService 객체: {} @@@", subscriptionService);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

            log.info("@@@ 찾은 사용자 전체: {} @@@", user);
            log.info("@@@ 사용자 ID: {} @@@", user.getId());

            subscriptionService.createSubscription(user, request.getPlan());

            log.info("@@@ 테스트용 구독 생성 완료 @@@");
            return ResponseEntity.ok("구독이 성공적으로 생성되었습니다.");

        } catch (Exception e) {
            log.error("@@@ 오류 발생: {} @@@", e.getMessage());
            log.error("@@@ 스택 트레이스: @@@", e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 테스트용 구독 상태 확인
    @GetMapping("/{userId}/status")
    @Operation(
            summary = "테스트용 구독 상태 확인",
            description = "사용자 ID를 직접 지정하여 구독 상태를 확인합니다. (인증 불필요)")
    public ResponseEntity<Boolean> isSubscribedTest(@PathVariable Long userId) {
        log.info("@@@ 테스트용 구독 상태 확인 - UserId: {} @@@", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        boolean active = subscriptionService.hasValidSubscription(user);

        log.info("구독 상태: {}", active);
        return ResponseEntity.ok(active);
    }

    // 테스트용 구독 정보 조회
    @GetMapping("/{userId}/details")
    @Operation(
            summary = "테스트용 구독 정보 조회",
            description = "사용자 ID를 직접 지정하여 구독 정보를 조회합니다. (인증 불필요)")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class)))
    public ResponseEntity<SubscriptionResponse> getSubscriptionTest(@PathVariable Long userId) {
        log.info("@@@ 테스트용 구독 정보 조회 - UserId: {} @@@", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        SubscriptionResponse response = subscriptionService.getSubscriptionDetails(user);
        return ResponseEntity.ok(response);
    }

    // 테스트용 즉시 해지
    @DeleteMapping("/{userId}")
    @Operation(
            summary = "테스트용 구독 즉시 해지",
            description = "사용자 ID를 직접 지정하여 구독을 즉시 해지합니다. (인증 불필요)")
    public ResponseEntity<?> unsubscribeTest(@PathVariable Long userId) {
        log.info("@@@ 테스트용 구독 즉 시 해지 - UserId: {} @@@", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다. ID: " + userId));

            subscriptionService.unsubscribe(user);

            log.info("@@@ 구독 즉시 해지 완료 @@@");
            return ResponseEntity.ok("구독이 즉시 해지되었습니다.");
        } catch (Exception e) {
            log.error("@@@ 구독 즉시 해지 오류: {} @@@", e.getMessage(), e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 테스트용 해지 예약
    @PatchMapping("/{userId}/cancel")
    @Operation(
            summary = "테스트용 구독 해지 예약",
            description = "사용자 ID를 직접 지정하여 구독 해지를 예약합니다. 기간 만료까지 사용 가능 (인증 불필요)")
    public ResponseEntity<?> cancelSubscriptionTest(@PathVariable Long userId) {
        log.info("@@@ 테스트용 구독 해지 예약 - UserId: {} @@@", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

            subscriptionService.cancelSubscription(user);

            log.info("@@@ 구독 해지 예약 완료 @@@");
            return ResponseEntity.ok("구독 해지 요청 완료. 만료일까지는 사용 가능합니다.");

        } catch (Exception e) {
            log.error("@@@ 구독 해지 예약 오류: {} @@@", e.getMessage(), e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 테스트용 만료된 구독 조회 (관리자용)
    @GetMapping("/expired")
    @Operation(
            summary = "테스트용 만료된 구독 조회",
            description = "현재 만료되어야 할 모든 구독 목록을 조회합니다. (관리자/테스트용)")
    public ResponseEntity<?> getExpiredSubscriptionsTest() {
        log.info("@@@ 테스트용 만료된 구독 조회 @@@");

        try {
            var expiredSubscriptions = subscriptionService.getExpiredSubscriptions();

            log.info("@@@ 만료된 구독 개수: {} @@@", expiredSubscriptions.size());
            return ResponseEntity.ok(expiredSubscriptions);

        } catch (Exception e) {
            log.error("@@@ 만료된 구독 조회 오류: {} @@@", e.getMessage(), e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 테스트용 수동 만료 처리 실행
    @PostMapping("/expire")
    @Operation(
            summary = "테스트용 수동 만료 처리",
            description = "스케줄러를 기다리지 않고 수동으로 만료 처리를 실행합니다. (테스트용)")
    public ResponseEntity<?> expireSubscriptionsTest() {
        log.info("@@@ 테스트용 수동 만료 처리 실행 @@@");

        try {
            subscriptionService.expireSubscriptions();

            log.info("@@@ 수동 만료 처리 완료 @@@");
            return ResponseEntity.ok("만료 처리가 완료되었습니다.");

        } catch (Exception e) {
            log.error("@@@ 수동 만료 처리 오류: {} @@@", e.getMessage(), e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 테스트용 사용자 목록 조회
    @GetMapping("/users")
    @Operation(
            summary = "테스트용 사용자 목록 조회",
            description = "테스트에 사용할 수 있는 사용자 목록을 조회합니다.")
    public ResponseEntity<?> getUsersTest() {
        log.info("@@@ 테스트용 사용자 목록 조회 @@@");

        try {
            var users = userRepository.findAll();

            // 간단한 정보만 반환 (ID, 이메일 등)
            var userInfos = users.stream()
                    .map(user -> "ID: " + user.getId() + ", Email: " + user.getEmail())
                    .toList();

            log.info("@@@ 사용자 수: {} @@@", users.size());
            return ResponseEntity.ok(userInfos);

        } catch (Exception e) {
            log.error("@@@ 사용자 목록 조회 오류: {} @@@", e.getMessage(), e);
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }


}
