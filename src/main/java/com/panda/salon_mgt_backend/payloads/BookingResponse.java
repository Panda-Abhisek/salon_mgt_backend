package com.panda.salon_mgt_backend.payloads;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long serviceId,
        String serviceName,
        Long staffId,
        String staffName,
        Long customerId,
        String customerName,
        Instant startTime,
        Instant endTime,
        String status
) {}

