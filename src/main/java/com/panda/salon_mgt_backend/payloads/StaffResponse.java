package com.panda.salon_mgt_backend.payloads;

import java.util.Set;

public record StaffResponse(
        Long id,
        String name,
        String email,
        boolean enabled,
        Set<String> roles,
        Set<ServiceResponse> services
) {}
