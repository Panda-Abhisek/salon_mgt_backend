package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.payloads.BillingMetricsResponse;
import com.panda.salon_mgt_backend.payloads.BillingTransactionDto;

import java.util.List;

public interface BillingMetricsService {
    BillingMetricsResponse getMetrics();

    List<BillingTransactionDto> recent();
//    long totalPayments = success + failed;
//    double successRate = totalPayments == 0 ? 0 :
//            (double) success / totalPayments;
}
