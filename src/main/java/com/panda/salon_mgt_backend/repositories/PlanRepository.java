package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByType(PlanType type);
}