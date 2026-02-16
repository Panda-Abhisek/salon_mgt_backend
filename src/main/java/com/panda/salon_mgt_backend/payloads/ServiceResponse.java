package com.panda.salon_mgt_backend.payloads;

import java.math.BigDecimal;

public record ServiceResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer durationMinutes,
        boolean active
) {
}
