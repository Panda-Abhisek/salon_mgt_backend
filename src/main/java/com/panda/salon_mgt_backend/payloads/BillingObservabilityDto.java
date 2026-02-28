package com.panda.salon_mgt_backend.payloads;

import lombok.Builder;

@Builder
public record BillingObservabilityDto(
        long pending,
        long deadLetters,
        long recoveredToday,
        double recoveryRate,
        long atRisk
) {}