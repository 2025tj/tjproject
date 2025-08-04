package com.tj.tjp.domain.subscription.controller;

import com.tj.tjp.domain.auth.security.principal.LocalUserPrincipal;
import com.tj.tjp.domain.subscription.dto.PaymentCompleteRequest;
import com.tj.tjp.domain.subscription.dto.PaymentResponse;
import com.tj.tjp.domain.subscription.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(
            @AuthenticationPrincipal LocalUserPrincipal principal,
            @RequestBody PaymentCompleteRequest request
            ) {
        PaymentResponse response = paymentService.completePayment(principal.getUser(), request);
        return ResponseEntity.ok(response);
    }
}
