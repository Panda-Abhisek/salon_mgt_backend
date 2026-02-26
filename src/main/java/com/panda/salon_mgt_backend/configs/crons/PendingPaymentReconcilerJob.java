package com.panda.salon_mgt_backend.configs.crons;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingPaymentReconcilerJob {

    private final BillingTransactionRepository billingRepo;

    @Scheduled(fixedDelay = 600_000) // every 10 mins
    @Transactional
    public void reconcilePendingPayments() {

        Instant cutoff = Instant.now().minusSeconds(600);

        List<BillingTransaction> pending =
                billingRepo.findAll().stream()
                        .filter(tx ->
                                tx.getStatus() == BillingStatus.PENDING &&
                                tx.getCreatedAt().isBefore(cutoff))
                        .toList();

        if (pending.isEmpty()) return;

        log.warn("billing.reconcile.pending count={}", pending.size());

        for (BillingTransaction tx : pending) {
            log.warn("billing.reconcile.flagged txId={}", tx.getId());
            // Later: call Stripe API here
        }
    }
}