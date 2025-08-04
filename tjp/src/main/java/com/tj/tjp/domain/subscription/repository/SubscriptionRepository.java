package com.tj.tjp.domain.subscription.repository;

import com.tj.tjp.domain.subscription.entity.Subscription;
import com.tj.tjp.domain.subscription.entity.SubscriptionStatus;
import com.tj.tjp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndIsActiveTrue(User user);

    Optional<Subscription> findByUserAndStatusIn(User user, List<SubscriptionStatus> statuses);

    List<Subscription> findByEndDateBeforeAndStatusIn(LocalDateTime now, List<SubscriptionStatus> statuses);

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status IN :statuses")
    List<Subscription> findExpiredSubscriptions(LocalDateTime now, List<SubscriptionStatus> statuses);

    Optional<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);
}
