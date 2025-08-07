package com.tj.tjp.domain.admin.controller;

import com.tj.tjp.common.dto.ApiResponse;
import com.tj.tjp.domain.subscription.dto.RefundRequestDto;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
import com.tj.tjp.domain.subscription.entity.RefundRequest;
import com.tj.tjp.domain.subscription.entity.RefundStatus;
import com.tj.tjp.domain.subscription.repository.RefundRequestRepository;
import com.tj.tjp.domain.subscription.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;
    private final RefundRequestRepository refundRequestRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundRequestDto>>> getRefundsRequests(
            @RequestParam(required = false) RefundStatus status
    ) {
        List<RefundRequest> requests = (status == null)
                ? refundRequestRepository.findAll()
                : refundRequestRepository.findByStatus(status);
        List<RefundRequestDto> dtos = requests.stream()
                .map(RefundRequestDto::from)
                .toList();
        log.info("dtos={}, ", dtos);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> approveRefund(
            @PathVariable Long id,
            @RequestParam Integer amount
    ) {
        SubscriptionStatusResponse response = refundService.approveRefund(id, amount);
        return ResponseEntity.ok(ApiResponse.success("환불 승인 완료", response));
    }
}
