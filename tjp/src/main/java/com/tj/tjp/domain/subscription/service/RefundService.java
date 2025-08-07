package com.tj.tjp.domain.subscription.service;

import com.tj.tjp.domain.subscription.dto.RefundRequestDto;
import com.tj.tjp.domain.subscription.dto.SubscriptionStatusResponse;
import com.tj.tjp.domain.subscription.entity.RefundRequest;
import com.tj.tjp.domain.subscription.repository.RefundRequestRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class RefundService {
    private final SubscriptionService subscriptionService;
    private final RefundRequestRepository refundRequestRepository;

    @Transactional
    public SubscriptionStatusResponse approveRefund(Long requestId, Integer amount) {
        RefundRequest request = refundRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("환불 요청을 찾을 수 없습니다."));

        SubscriptionStatusResponse response =
                subscriptionService.unsubscribeAndRefund(request.getUser(), amount);
        request.approve(amount);
        refundRequestRepository.save(request);
        return response;
    }

    @Transactional(readOnly = true)
    public List<RefundRequestDto> getAllRefundRequests() {
        return refundRequestRepository.findAll().stream()
                .map(RefundRequestDto::from)
                .toList();

    }
}
