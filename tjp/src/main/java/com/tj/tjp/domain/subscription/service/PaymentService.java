package com.tj.tjp.domain.subscription.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.tj.tjp.domain.subscription.dto.PaymentCompleteRequest;
import com.tj.tjp.domain.subscription.dto.PaymentResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionResponse;
import com.tj.tjp.domain.subscription.entity.*;
import com.tj.tjp.domain.subscription.repository.PaymentRepository;
import com.tj.tjp.domain.subscription.repository.PlanRepository;
import com.tj.tjp.domain.subscription.repository.SubscriptionRepository;
import com.tj.tjp.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;
    private final IamportClient iamportClient;

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
}
