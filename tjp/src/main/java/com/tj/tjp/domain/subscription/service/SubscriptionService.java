package com.tj.tjp.domain.subscription.service;

import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.entity.Plan;
import com.tj.tjp.domain.subscription.entity.PlanType;
import com.tj.tjp.domain.subscription.entity.Subscription;
import com.tj.tjp.domain.subscription.entity.SubscriptionStatus;
import com.tj.tjp.domain.subscription.repository.PlanRepository;
import com.tj.tjp.domain.subscription.repository.SubscriptionRepository;
import com.tj.tjp.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

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

    public SubscriptionResponse getSubscriptionDetails(User user) {
        return subscriptionRepository.findByUserAndIsActiveTrue(user)
                .filter(Subscription::isValid)
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new RuntimeException("구독 정보가 없습니다."));
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
}
