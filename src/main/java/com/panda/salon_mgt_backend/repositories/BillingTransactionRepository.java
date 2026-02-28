package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, Long> {
    List<BillingTransaction> findBySalonOrderByCreatedAtDesc(Salon salon);

    Optional<BillingTransaction> findByExternalOrderId(String externalOrderId);

    List<BillingTransaction> findTop20ByOrderByCreatedAtDesc();

    boolean existsBySalonAndStatus(Salon salon, BillingStatus status);

    List<BillingTransaction> findByStatusAndCreatedAtBefore(
            BillingStatus status,
            Instant cutoff
    );

    List<BillingTransaction> findTop50ByStatusOrderByCreatedAtDesc(BillingStatus status);

    long countByStatus(BillingStatus status);

    @Query("""
                SELECT COUNT(b)
                FROM BillingTransaction b
                WHERE b.status = 'PAID'
                AND b.completedAt >= :since
            """)
    long countRecoveredSince(@Param("since") Instant since);

    @Query("""
                SELECT COUNT(b)
                FROM BillingTransaction b
                WHERE b.status = 'FAILED_PERMANENT'
            """)
    long countDeadLetters();
}