package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.PlanType;
import jakarta.validation.constraints.NotNull;

public record UpgradePlanRequest(
        @NotNull PlanType plan
) {}