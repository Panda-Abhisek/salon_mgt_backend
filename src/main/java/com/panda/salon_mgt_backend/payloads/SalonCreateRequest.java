package com.panda.salon_mgt_backend.payloads;

public record SalonCreateRequest(
        String salonName,
        String salonAddress
) {}
