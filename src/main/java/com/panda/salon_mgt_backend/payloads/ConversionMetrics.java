package com.panda.salon_mgt_backend.payloads;

public record ConversionMetrics(
        long activeTrials,
        long trialsEndingSoon,
        long conversions7d,
        double conversionRate
) {}