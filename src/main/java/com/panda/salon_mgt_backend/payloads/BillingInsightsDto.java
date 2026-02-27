package com.panda.salon_mgt_backend.payloads;

public record BillingInsightsDto(
        long activePaid,
        long inGrace,
        long delinquent,
        long atRisk
) {}