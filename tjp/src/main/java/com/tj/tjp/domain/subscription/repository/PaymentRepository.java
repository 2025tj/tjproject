package com.tj.tjp.domain.subscription.repository;

import com.tj.tjp.domain.subscription.entity.Payment;
import com.tj.tjp.domain.subscription.entity.Plan;
import com.tj.tjp.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findBySubscription(Subscription subscription);

}
