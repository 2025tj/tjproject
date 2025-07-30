package com.tj.tjp.domain.subscription.repository;

import com.tj.tjp.domain.subscription.entity.Plan;
import com.tj.tjp.domain.subscription.entity.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(PlanType name);
}
