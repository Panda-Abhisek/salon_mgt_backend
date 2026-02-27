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

    private final BillingTransactionRepository billingRepo;
    private final BillingService billingService;

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
                    log.error("🔥 billing.recovered_and_activated txId={}", tx.getId());
                } else {
                    log.warn("billing.reconcile.still_pending txId={}", tx.getId());
                }

                return; // success or still pending

            } catch (Exception ex) {
                attempts++;

                log.warn("billing.reconcile.retry txId={} attempt={}", tx.getId(), attempts);

                try {
                    Thread.sleep(500L * attempts); // simple backoff
                } catch (InterruptedException ignored) {}
            }
        }

        log.error("billing.reconcile.permanent_failure txId={}", tx.getId());
    }
}