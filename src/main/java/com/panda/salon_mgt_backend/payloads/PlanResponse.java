package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.PlanType;

public record PlanResponse(
        PlanType type,
        String name,
        Integer maxStaff,
        Integer maxServices,
        Boolean analyticsEnabled,
        Boolean smartAlertsEnabled,
        Integer priceMonthly
) {}