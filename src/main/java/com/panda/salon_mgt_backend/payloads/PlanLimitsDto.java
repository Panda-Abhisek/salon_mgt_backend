package com.panda.salon_mgt_backend.payloads;

public record PlanLimitsDto(
        Integer maxStaff,
        Integer maxServices,
        Integer maxBookings
) {}