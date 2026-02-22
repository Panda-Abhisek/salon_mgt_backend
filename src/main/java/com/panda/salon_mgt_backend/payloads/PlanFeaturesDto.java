package com.panda.salon_mgt_backend.payloads;

public record PlanFeaturesDto(
        Boolean analyticsEnabled,
        Boolean smartAlertsEnabled
) {}