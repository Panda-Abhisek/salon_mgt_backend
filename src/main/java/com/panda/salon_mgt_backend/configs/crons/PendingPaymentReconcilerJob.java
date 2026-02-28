package com.panda.salon_mgt_backend.configs.crons;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.services.BillingService;
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
    private static final int MAX_RETRIES = 10;
    private final BillingTransactionRepository billingRepo;
    private final BillingService billingService;

    private long computeBackoffSeconds(int retryCount) {
        // 2^n exponential, capped
        long seconds = (long) Math.pow(2, retryCount) * 60;
        return Math.min(seconds, 3600); // max 1 hour
    }

    @Scheduled(fixedDelay = 600_000) // every 10 mins
    @Transactional
    public void reconcilePendingPayments() {

        Instant cutoff = Instant.now().minusSeconds(600);

        List<BillingTransaction> pending =
                billingRepo.findByStatusAndCreatedAtBefore(
                        BillingStatus.PENDING,
                        cutoff
                );

        if (pending.isEmpty()) return;

        log.warn("billing.reconcile.pending count={}", pending.size());

        for (BillingTransaction tx : pending) {

            // 🧠 Skip dead letters
            if (tx.getStatus() == BillingStatus.FAILED_PERMANENT) {
                continue;
            }

            int retries = tx.getRetryCount() == null ? 0 : tx.getRetryCount();

            // 🛑 Retry ceiling
            if (retries >= MAX_RETRIES) {
                tx.setStatus(BillingStatus.FAILED_PERMANENT);
                tx.setLastFailureReason("Retry ceiling exceeded");
                billingRepo.save(tx);

                log.error("billing.dead_lettered.retry_ceiling txId={}", tx.getId());
                continue;
            }

            // ⏳ Exponential backoff skip
            if (tx.getLastRetryAt() != null) {
                long waitSeconds = computeBackoffSeconds(retries);
                Instant nextAllowed = tx.getLastRetryAt().plusSeconds(waitSeconds);

                if (Instant.now().isBefore(nextAllowed)) {
                    log.debug("billing.reconcile.backoff_skip txId={} nextRetryAt={}",
                            tx.getId(),
                            nextAllowed);
                    continue;
                }
            }

            try {
                reconcile(tx);
            } catch (Exception e) {
                log.error("billing.reconcile.failed txId={}", tx.getId(), e);
            }
        }
    }

    private void reconcile(BillingTransaction tx) {

        String sessionId = tx.getExternalOrderId();

        int attempts = 0;
        while (attempts < 3) {
            try {
                com.stripe.model.checkout.Session session =
                        com.stripe.model.checkout.Session.retrieve(sessionId);

                if ("complete".equals(session.getStatus())) {
                    billingService.handleRecoveredPayment(tx);
                    log.warn("billing.recovered txId={}", tx.getId());
                } else {
                    log.warn("billing.reconcile.still_pending txId={}", tx.getId());
                }

                return; // success or still pending

            } catch (Exception ex) {
                attempts++;

                tx.setRetryCount((tx.getRetryCount() == null ? 0 : tx.getRetryCount()) + 1);
                tx.setLastRetryAt(Instant.now());
                tx.setLastFailureReason(ex.getMessage());

                billingRepo.save(tx);

                log.warn("billing.reconcile.retry txId={} attempt={} totalRetries={}",
                        tx.getId(),
                        attempts,
                        tx.getRetryCount());

            }
        }

        tx.setStatus(BillingStatus.FAILED_PERMANENT);
        tx.setLastFailureReason("Exceeded reconciliation retries");
        tx.setLastRetryAt(Instant.now());
        billingRepo.save(tx);

        log.error("billing.dead_lettered txId={}", tx.getId());
    }
}