package com.panda.salon_mgt_backend.payloads;

import java.time.OffsetDateTime;

public record TimeSlot(
        OffsetDateTime start,
        OffsetDateTime end
) {}
