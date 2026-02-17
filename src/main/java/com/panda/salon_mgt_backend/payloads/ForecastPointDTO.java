package com.panda.salon_mgt_backend.payloads;

import java.time.LocalDate;

public record ForecastPointDTO(
        LocalDate date,
        long value
) {}
