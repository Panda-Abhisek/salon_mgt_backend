package com.panda.salon_mgt_backend.payloads;

public record SalonResponse(
        Long id,
        String salonName,
        String salonAddress
) {}
