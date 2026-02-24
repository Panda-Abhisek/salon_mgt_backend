package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.payloads.BillingMetricsResponse;
import com.panda.salon_mgt_backend.payloads.BillingTransactionDto;
import com.panda.salon_mgt_backend.payloads.ChurnDto;
import com.panda.salon_mgt_backend.payloads.ConversionMetrics;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingMetricsServiceImpl implements BillingMetricsService {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingTransactionRepository billingRepo;

    @Override
    public List<BillingTransactionDto> recent() {
        return billingRepo
                .findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(BillingTransactionDto::from)
                .toList();
    }

    @Override
    public BillingMetricsResponse getMetrics() {
        try {
            long totalActive = subscriptionRepository.countTotalActive();

            Map<PlanType, Long> activeByPlan = mapActiveByPlan();

            Instant now = Instant.now();
            Instant expiringCutoff = now.plus(7, ChronoUnit.DAYS);
            Instant last7Days = now.minus(7, ChronoUnit.DAYS);

            long expiringSoon = subscriptionRepository.countExpiringBefore(expiringCutoff);
            long paidExpiredLast7Days = subscriptionRepository.countPaidExpiredSince(last7Days);

            long activePaid = subscriptionRepository.countActivePaid();
            ChurnDto churn = calculatePaidChurn(paidExpiredLast7Days, activePaid);

            Instant trialsSoonCutoff = now.plus(3, ChronoUnit.DAYS);

            long activeTrials = subscriptionRepository.countActiveTrials();
            long trialsEndingSoon = subscriptionRepository.countTrialsEndingBefore(trialsSoonCutoff);
            long conversions7d = subscriptionRepository.countPaidActivationsSince(last7Days);

            double conversionRate = activeTrials == 0
                    ? 0.0
                    : (double) conversions7d / activeTrials;

            ConversionMetrics conversion = new ConversionMetrics(
                    activeTrials,
                    trialsEndingSoon,
                    conversions7d,
                    Math.round(conversionRate * 100.0) / 100.0
            );

            return new BillingMetricsResponse(
                    totalActive,
                    activeByPlan,
                    expiringSoon,
                    paidExpiredLast7Days,
                    churn,
                    conversion
            );

        } catch (Exception ex) {
            log.warn("billing.metrics.failed", ex);
            return emptyMetrics();
        }
    }

    private Map<PlanType, Long> mapActiveByPlan() {
        Map<PlanType, Long> map = new EnumMap<>(PlanType.class);

        for (Object[] row : subscriptionRepository.countActiveByPlan()) {
            PlanType plan = (PlanType) row[0];
            Long count = (Long) row[1];
            map.put(plan, count);
        }

        return map;
    }

    private ChurnDto calculatePaidChurn(long expiredPaid, long activePaid) {
        if (activePaid == 0) {
            return new ChurnDto(0, 0.0);
        }

        double rate = (double) expiredPaid / activePaid;
        return new ChurnDto(expiredPaid, Math.round(rate * 100.0) / 100.0);
    }

    private BillingMetricsResponse emptyMetrics() {
        return new BillingMetricsResponse(
                0,
                Map.of(),
                0,
                0,
                new ChurnDto(0, 0.0),
                new ConversionMetrics(0, 0, 0, 0.0)
        );
    }
}