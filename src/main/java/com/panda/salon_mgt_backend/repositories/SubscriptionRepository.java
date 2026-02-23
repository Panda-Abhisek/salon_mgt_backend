package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySalonAndStatus(Salon salon, SubscriptionStatus subscriptionStatus);

    @Query("""
            SELECT s.plan.type, COUNT(s)
            FROM Subscription s
            WHERE s.status = 'ACTIVE'
            GROUP BY s.plan.type
            """)
    List<Object[]> countActiveByPlan();

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'ACTIVE'
            """)
    long countTotalActive();

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'ACTIVE'
            AND s.endDate <= :cutoff
            """)
    long countExpiringBefore(@Param("cutoff") Instant cutoff);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'EXPIRED'
            AND s.endDate >= :since
            """)
    long countExpiredSince(@Param("since") Instant since);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'EXPIRED'
            AND s.endDate >= :since
            AND s.plan.type <> 'FREE'
            """)
    long countPaidExpiredSince(@Param("since") Instant since);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'ACTIVE'
            AND s.plan.type <> 'FREE'
            """)
    long countActivePaid();

    Optional<Subscription> findTopBySalonAndStatusInOrderByStartDateDesc(
            Salon salon,
            List<SubscriptionStatus> statuses
    );

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'TRIAL'
            """)
    long countActiveTrials();

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'TRIAL'
            AND s.endDate <= :cutoff
            """)
    long countTrialsEndingBefore(@Param("cutoff") Instant cutoff);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = 'ACTIVE'
            AND s.plan.type <> 'FREE'
            AND s.startDate >= :since
            """)
    long countPaidActivationsSince(@Param("since") Instant since);

    @Query("""
                SELECT COUNT(s) > 0
                FROM Subscription s
                WHERE s.salon = :salon
                AND s.status = 'TRIAL'
            """)
    boolean hasUsedTrial(@Param("salon") Salon salon);
}