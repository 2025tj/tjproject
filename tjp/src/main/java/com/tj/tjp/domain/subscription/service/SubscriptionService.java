package com.tj.tjp.domain.subscription.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
import com.tj.tjp.domain.subscription.entity.*;
import com.tj.tjp.domain.subscription.repository.PaymentRepository;
import com.tj.tjp.domain.subscription.repository.PlanRepository;
import com.tj.tjp.domain.subscription.repository.RefundRequestRepository;
import com.tj.tjp.domain.subscription.repository.SubscriptionRepository;
import com.tj.tjp.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final IamportClient iamportClient;
    private final PaymentRepository paymentRepository;
    private final RefundRequestRepository refundRequestRepository;

    // 구독 생성
    public Subscription createSubscription(User user, PlanType planType) {
        log.info("=== createSubscription 디버깅 ===");
        log.info("User: {}", user != null ? user.getId() : "null");
        log.info("PlanType: {}", planType);

        Plan plan = planRepository.findByName(planType)
                .orElseThrow(() -> {
                    log.error("Plan을 찾을 수 없음: {}", planType);
                    return new IllegalArgumentException("존재하지 않는 요금제입니다: " + planType);
                });

        log.info("조회된 Plan - ID: {}, Name: {}, Price: {}", plan.getId(), plan.getName(), plan.getPrice());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(plan.getName().getDurationDays());

        // 기존 구독 비활성화
        subscriptionRepository.findByUserAndIsActiveTrue(user).ifPresent(oldSub -> {
            oldSub.deactivate();
            oldSub.expire();
            subscriptionRepository.save(oldSub);
        });

        Subscription subscription = Subscription.of(user, plan, now, end);
        log.info("생성될 Subscription - User: {}, Plan: {}, Start: {}, End: {}",
                subscription.getUser().getId(),
                subscription.getPlan() != null ? subscription.getPlan().getId() : "null",
                subscription.getStartDate(),
                subscription.getEndDate());

        return subscriptionRepository.save(subscription);
    }

    // 구독 활성 여부 확인
    public boolean hasValidSubscription(User user) {
        return subscriptionRepository.findByUserAndIsActiveTrue(user)
                .map(subscription -> {
                    if (!subscription.isValid()) {
                        subscription.expire();
                        subscriptionRepository.save(subscription);
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

//    public SubscriptionResponse getSubscriptionDetails(User user) {
//        log.info("[getSubscriptionDetails] user={}", user);
//        return subscriptionRepository.findByUserAndIsActiveTrue(user)
//                .filter(sub -> {
//                    log.info(">> Subscription 조회 결과: id={}, plan={}",
//                            sub.getId(),
//                            sub.getPlan() != null? sub.getPlan().getName(): null);
//                    return sub.isValid();
//                })
//                .map(sub -> {
//                    log.info(">> 유효한 구독 변환 시작: id={}", sub.getId());
//                    return SubscriptionResponse.from(sub);
//                })
//                .orElseThrow(() -> {
//                    log.warn(">> 유효한 구독 정보 없음 (user={})", user.getId());
//                    return new RuntimeException("구독 정보가 없습니다.");
//                });
////                .filter(Subscription::isValid)
////                .map(SubscriptionResponse::from)
////                .orElseThrow(() -> new RuntimeException("구독 정보가 없습니다."));
//    }
    public SubscriptionResponse getSubscriptionDetails(User user) {
        log.info("[getSubscriptionDetails] user={}", user);
        return subscriptionRepository.findByUserAndIsActiveTrue(user)
                .filter(sub -> {
                    log.info(">> Subscription 조회 결과: id={}, plan={}",
                            sub.getId(),
                            sub.getPlan() != null? sub.getPlan().getName(): null);
                    return sub.isValid();
                })
                .map(sub -> {
                    log.info(">> 유효한 구독 변환 시작: id={}", sub.getId());
                    return SubscriptionResponse.from(sub);
                })
                .orElseGet(() -> {
                    log.warn(">> 유효한 구독 정보 없음 (user={})", user.getId());
                    return null;
                });
    }

    // 즉시 해지 (환불 등)
    public void unsubscribe(User user) {
        subscriptionRepository.findByUserAndIsActiveTrue(user)
                .ifPresent(subscription -> {
                    subscription.deactivate();
                    subscription.expire();
                    subscriptionRepository.save(subscription);
                });
    }

    // 기간 만료 후 해지 (일반적인 해지)
    public void cancelSubscription(User user) {
        Subscription subscription = subscriptionRepository.findByUserAndStatusIn(
                user, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED))
                .orElseThrow(() -> new RuntimeException("유효한 구독이 없습니다."));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new RuntimeException("이미 해지 요청된 구독입니다.");
        }

        log.info("구독 해지 요청 - User: {}, 만료일까지 사용 가능: {}",
                user.getId(), subscription.getEndDate());

        subscription.cancel();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void revertCancel(User user) {
        Subscription sub = subscriptionRepository
                .findByUserAndStatus(user, SubscriptionStatus.CANCELLED)
                .orElseThrow(() -> new RuntimeException("활성 구독 없음"));

        if (!sub.getStatus().equals(SubscriptionStatus.CANCELLED)) {
            throw new RuntimeException("해지 예약 상태가 아님");
        }
        sub.revertCancel();
        subscriptionRepository.save(sub);
    }

    // 기존 활성 구독 비활성화 (헬퍼 메서드)
    private void deactivateExistingSubscription(User user) {
        subscriptionRepository.findByUserAndIsActiveTrue(user)
                .ifPresent(oldSubscription -> {
                    log.info("기존 구독 비활성화 - User: {}, Old Subscription: {}",
                            user.getId(), oldSubscription.getId());
                    oldSubscription.deactivate();
                    oldSubscription.expire();
                    subscriptionRepository.save(oldSubscription);
                });
    }

    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다
    @Transactional
    public void expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        // ACTIVE 또는 CANCELLED 상태인데 만료일이 지난 구독들 찾기
        List<Subscription> expired = subscriptionRepository.findByEndDateBeforeAndStatusIn(now,
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED));

        if (!expired.isEmpty()) {
            log.info("만료된 구독 {} 건 처리 시작", expired.size());

            for (Subscription subscription : expired) {
                log.info("구독 만료 처리 - User: {}, Subscription: {}, EndDate: {}",
                        subscription.getUser().getId(),
                        subscription.getId(),
                        subscription.getEndDate());

                subscription.expire();
            }

            subscriptionRepository.saveAll(expired);
            log.info("만료된 구독 {} 건 처리 완료", expired.size());
        }
    }

    // 구독 상태 확인 (관리자용)
    @Transactional(readOnly = true)
    public List<Subscription> getExpiredSubscriptions() {
        return subscriptionRepository.findByEndDateBeforeAndStatusIn(
                LocalDateTime.now(),
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED));
    }

    @Transactional
    public SubscriptionStatusResponse unsubscribeAndRefund(User user, Integer cancelAmount) {
        Subscription subscription = subscriptionRepository.findByUserAndIsActiveTrue(user)
                .orElse(null);
        if (subscription == null) return SubscriptionStatusResponse.none();
        Payment payment = paymentRepository.findBySubscription(subscription)
                .orElseThrow(() -> new RuntimeException("구독 결제 내역을 찾을 수 없습니다."));
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return SubscriptionStatusResponse.from(subscription);
        }
        //아임 포트 환불처리
        BigDecimal refundAmount = (cancelAmount != null) ? BigDecimal.valueOf(cancelAmount) : null;
        CancelData cancelData = new CancelData(payment.getImpUid(), true, refundAmount);
        try {
            IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse =
                    iamportClient.cancelPaymentByImpUid(cancelData);
            if (cancelResponse == null) {
                throw new RuntimeException("아임포트 환불 실패: 응답이 null입니다.");
            }
            if (cancelResponse.getResponse() == null) {
                throw new RuntimeException("아임 포트 환불 실패: 응답 없음");
            }
            String status = cancelResponse.getResponse().getStatus();
            if (!"cancelled".equalsIgnoreCase(status)) {
                throw new RuntimeException("아임포트 환불 실패: 상태=" + status);
            }

            payment.updateStatus(PaymentStatus.CANCELLED);
            subscription.expire();

            log.info("[Unsubscribe+Refund] user={}, status=CANCELLED", user.getEmail());
            return SubscriptionStatusResponse.from(subscription);
        } catch (Exception e) {
            log.error("[Refund] 아임포트 환불 처리 중 예외 발생", e);
            throw new RuntimeException("환불 처리 실패: "+e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(User user) {
        Subscription subscription = subscriptionRepository.findByUserAndIsActiveTrue(user)
                .orElse(null);
        return SubscriptionStatusResponse.from(subscription);
    }

    @Transactional
    public void createRefundRequest(User user) {
        Subscription sub = subscriptionRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new RuntimeException("활성 구독 없음"));

        // 기존 요청 중복 장지
        if (refundRequestRepository.existsBySubscriptionAndStatus(sub, RefundStatus.REQUESTED)) {
            throw new RuntimeException("이미 환불 요청이 존재합니다.");
        }

        RefundRequest request = RefundRequest.builder()
                .user(user)
                .subscription(sub)
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        refundRequestRepository.save(request);
        sub.cancel();
    }
}
