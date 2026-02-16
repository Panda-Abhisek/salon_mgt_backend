package com.panda.salon_mgt_backend.services.analytics;

import java.time.LocalDate;

public record TrendPointDTO(
        LocalDate date,
        long value
) {}
