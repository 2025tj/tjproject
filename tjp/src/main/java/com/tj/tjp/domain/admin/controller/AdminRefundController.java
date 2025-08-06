package com.tj.tjp.domain.admin.controller;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
import com.tj.tjp.domain.subscription.entity.RefundRequest;
import com.tj.tjp.domain.subscription.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> approveRefund(
            @PathVariable Long id,
            @RequestParam Integer amount
    ) {
        SubscriptionStatusResponse response = refundService.approveRefund(id, amount);
        return ResponseEntity.ok(ApiResponse.success("환불 승인 완료", response));
    }
}
