package com.panda.salon_mgt_backend.payloads;

import java.time.Instant;

public record CreateBookingRequest(
        Long serviceId,
        Long staffId,
        Long customerId,   // nullable for USER
        Instant startTime
) {}

