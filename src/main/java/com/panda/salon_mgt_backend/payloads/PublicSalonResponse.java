package com.panda.salon_mgt_backend.payloads;

public record PublicSalonResponse(
        Long id,
        String name,
        String address
) {}
