package com.tj.tjp.domain.subscription.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.tj.tjp.domain.subscription.dto.PaymentCompleteRequest;
import com.tj.tjp.domain.subscription.dto.PaymentResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.entity.*;
import com.tj.tjp.domain.subscription.repository.PaymentRepository;
import com.tj.tjp.domain.subscription.repository.PlanRepository;
import com.tj.tjp.domain.subscription.repository.SubscriptionRepository;
import com.tj.tjp.domain.user.entity.User;
import com.tj.tjp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;
    private final IamportClient iamportClient;
    private final UserRepository userRepository;

//    @Transactional
//    public PaymentResponse processPayment(User user, PaymentCompleteRequest req) {
//        Payment payment = Payment.builder()
//                .user(user)
//                .merchantUid(req.getMerchantUid())
//                .impUid(req.getImpUid())
//                .amount(req.getAmount())
//                .status(PaymentStatus.PAID)
//                .paidAt(LocalDateTime.now())
//                .build();
//
//        Plan plan = planRepository.findByNameIgnoreCase(req.getPlanType())
//                .orElseThrow(()-> new RuntimeException("플랜 없음"));
//
//        Subscription subscription = Subscription.of(
//                user,
//                plan,
//                LocalDateTime.now(),
//                LocalDateTime.now().plusMonths(1)
//        );
//
//        subscriptionRepository.save(subscription);
//
//        payment.setSubScription(subscription);
//        paymentRepository.save(payment);
//
//        return new PaymentResponse(subscription, payment);
//
//    }

    @Transactional
    public PaymentResponse completePayment(User user, PaymentCompleteRequest req) {
        paymentRepository.findByImpUid(req.getImpUid())
                .ifPresent(p -> { throw new RuntimeException("이미 처리된 결제입니다.");});

        // 아임포트 결제 검증
        try {
            IamportResponse<com.siot.IamportRestClient.response.Payment> paymentResponse = iamportClient.paymentByImpUid(req.getImpUid());

            if (paymentResponse.getResponse() == null) {
                throw new RuntimeException("결제 정보를 찾을 수 없습니다.");
            }
            int paidAmount = paymentResponse.getResponse().getAmount().intValue();
            if (paidAmount != req.getAmount()) {
                throw new RuntimeException("결제 금액 불일치: 실제=" + paidAmount + ", 요청=" + req.getAmount());
            }

            // (선택) 결제 상태 확인
            if (!"paid".equalsIgnoreCase(String.valueOf(paymentResponse.getResponse().getStatus()))) {
                throw new RuntimeException("결제 상태가 완료가 아닙니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("결제 검증 실패: " + e.getMessage(), e);
        }

        Subscription subscription = subscriptionService.createSubscription(user, req.getPlanType());

        Payment payment = Payment.of(user, subscription, req.getImpUid(), req.getMerchantUid(), req.getAmount(), PaymentStatus.PAID);
        paymentRepository.save(payment);

        log.info("결제 완료 - user={}, amount={}, plan={}", user.getEmail(), payment.getAmount(), subscription.getPlan().getName());
        return PaymentResponse.from(subscription, payment);
    }

    /**
     * 웹훅/검증 결과로 DB 상태 동기화
     */
    @Transactional
    public void syncPaymentStatus(com.siot.IamportRestClient.response.Payment iamportPayment) {
        String impUid = iamportPayment.getImpUid();
        String status = iamportPayment.getStatus();

        paymentRepository.findByImpUid(impUid).ifPresentOrElse(payment -> {
            // DB상태 업데이트
            switch (status.toLowerCase()) {
                case "paid" -> payment.markPaid();
                case "cancelled" -> payment.markCancelled();
                case "failed" -> payment.markFailed();
                default -> log.warn("알 수 없는 결제 상태: {}", status);
            }
            log.info("결제 상태 동기화 완료: impUid={}, status={}", impUid, status);
        }, () -> {
            log.warn("결제 내역 없음, impUid={} -> 신규 생성", impUid);
        });
    }

    @Transactional
    public void syncPaymentStatusWithFallback(com.siot.IamportRestClient.response.Payment iamportPayment) {
        String impUid = iamportPayment.getImpUid();
        String statusStr = iamportPayment.getStatus();
        PaymentStatus status = PaymentStatus.from(statusStr);

        paymentRepository.findByImpUid(impUid).ifPresentOrElse(payment -> {
            // DB상태 업데이트
            payment.updateStatus(status);
            log.info("결제 상태 동기화 완료: impUid={}, status={}", impUid, status);
        }, () -> {
            log.warn("결제 내역 없음, impUid={} -> 신규 생성", impUid);

            // 사용자 식별( 아임포터 buyer_email 또는 merchant_uid 기반)
            String email = iamportPayment.getBuyerEmail();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.error("Webhook: 사용자 식별 실패 impUid={}", impUid);
                return; // or throw new RuntimeException
            }

            User user = userOpt.get();

            // 구독 생성
            String planName = iamportPayment.getName().split(" ")[0];
            PlanType planType = PlanType.from(planName);
            Subscription subscription = subscriptionService.createSubscription(user, planType);

            // 결제 생성
            Payment payment = Payment.of(
                    user,
                    subscription,
                    impUid,
                    iamportPayment.getMerchantUid(),
                    iamportPayment.getAmount().intValue(),
                    status
            );
            paymentRepository.save(payment);
            log.info("[webhook-Create-Success] impUid={}, status={}, user={}", impUid, status, user.getEmail());
        });
    }

    @Transactional
    public PaymentResponse refundPayment(String impUid, Integer cancelAmount) {
        Payment payment = paymentRepository.findByImpUid(impUid)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다."));

        // 이미 취소된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new RuntimeException("이미 취소된 결제입니다.");
        }

        try {
            // 환불 요청 데이터 준비
            BigDecimal refundAmount = (cancelAmount != null)
                    ? BigDecimal.valueOf(cancelAmount)
                    : null; // 전체 환불시 null

            // 아임포트 환불 요청(imp_uid 기준)
            CancelData cancelData = new CancelData(impUid, true, refundAmount); // imp_uid 기준 취소

            // 아임포트 API 호출
            IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

            if (cancelResponse.getResponse() == null) {
                throw new RuntimeException("아임포트 환불 실패: 응답 없음");
            }

            // DB 상태 업데이트
            payment.updateStatus(PaymentStatus.CANCELLED);
            log.info("[Refund] 결제 취소 완료: impUid={}, user={}",
                    impUid, payment.getUser().getEmail());

            // Subscription 상태 처리 ( 정기구독시 비활성화)
            Subscription subscription = payment.getSubscription();
            if (subscription != null) {
                subscription.expire();
                log.info("[Refund] 구독 비활성화 완료: user={}", payment.getUser().getEmail());
            }

            // webhook이 와도 최종 상태 동일 -> idmpotent
            return PaymentResponse.from(subscription, payment);
        } catch (Exception e) {
            throw new RuntimeException("환불 처리 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PaymentResponse unsubscribeAndRefund(User user, Integer cancelAmount) {
        // 활성 구독 조회
        Subscription subscription = subscriptionRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new RuntimeException("활성화된 구독이 없습니다."));

        // 결제 내역 조회
        Payment payment = paymentRepository.findBySubscription(subscription)
                .orElseThrow(() -> new RuntimeException("구독 결제 내역을 찾을 수 없습니다."));

        // 이미 취소된 결제라면 중복 처리 방지
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new RuntimeException("이미 취소된 결제입니다.");
        }

        // 아임포트 환불 요청
        try {
            // 환불 요청 데이터 준비
            BigDecimal refundAmount = (cancelAmount != null)
                    ? BigDecimal.valueOf(cancelAmount)
                    : null; // 전체 환불시 null

            // 아임포트 환불 요청(imp_uid 기준)
            CancelData cancelData = new CancelData(payment.getImpUid(), true, refundAmount); // imp_uid 기준 취소
//            CancelData cancelData = new CancelData(payment.getImpUid(), true, null); // 전체환불
            IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse =
                    iamportClient.cancelPaymentByImpUid(cancelData);
            if (cancelResponse.getResponse() == null) {
                throw new RuntimeException("아임 포트 환불 실패: 응답 없음");
            }

            // DB 상태 갱신
            payment.updateStatus(PaymentStatus.CANCELLED);
            subscription.expire(); // 구독 만료 처리
            log.info("[Unsubscribe+Refund] 구독 해지 & 결제 환불 완료: user={}, impUid={}",
                    user.getEmail(), payment.getImpUid());

            // webhook이 와도 상태는 동일 -> idempotent
            return PaymentResponse.from(subscription, payment);
        } catch (Exception e) {
            throw new RuntimeException("구독 해지/환불 처리 실패: " + e.getMessage(), e);
        }
    }
}
