package com.panda.salon_mgt_backend.payloads;

public record ChurnDto(
        long count,
        double rate
) {}