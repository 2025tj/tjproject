package com.tj.tjp.domain.subscription.repository;

import com.tj.tjp.domain.subscription.entity.RefundRequest;
import com.tj.tjp.domain.subscription.entity.RefundStatus;
import com.tj.tjp.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    boolean existsBySubscriptionAndStatus(Subscription subscription, RefundStatus status);

    List<RefundRequest> findByStatus(RefundStatus status);
}
