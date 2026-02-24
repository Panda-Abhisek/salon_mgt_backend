package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, Long> {
    List<BillingTransaction> findBySalonOrderByCreatedAtDesc(Salon salon);

    Optional<BillingTransaction> findByExternalOrderId(String externalOrderId);

    List<BillingTransaction> findTop20ByOrderByCreatedAtDesc();
}