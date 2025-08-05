package com.tj.tjp.domain.subscription.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.tj.tjp.domain.subscription.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentWebhookController {
    private final PaymentService paymentService;
    private final IamportClient iamportClient;

    /**
     * 아임포트 웹훅 수신 ( 운영 + 테스트 공용 )
     * 아임 포트 서버가 결제 상태 변화를 알려줄 때 호출됨
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("raw payload: {}", payload);
        try {
            String impUid = (String) payload.get("imp_uid");
            String status = (String) payload.get("status");

            log.info("웹 훅 수신: impUid={}, status={}", impUid, status);

            // 아임포트 서버로 최종 상태 검증
            IamportResponse<Payment> response = iamportClient.paymentByImpUid(impUid);
            Payment iamportPayment = response.getResponse();

            if (iamportPayment == null) {
                log.error("웹 훅 처리 실패: imp_uid={} 결제 정보 없음", impUid);
                return ResponseEntity.badRequest().body("fail: no payment info");
            }

            // 상태 동기화 (DB 업데이트)
//            paymentService.syncPaymentStatus(iamportPayment);
            paymentService.syncPaymentStatusWithFallback(iamportPayment);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("웹훅 처리중 오류", e);
            return ResponseEntity.internalServerError().body("fail: " + e.getMessage());
        }
    }
}
