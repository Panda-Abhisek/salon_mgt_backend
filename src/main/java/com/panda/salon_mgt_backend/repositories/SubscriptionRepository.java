package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}