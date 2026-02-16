package com.panda.salon_mgt_backend.payloads;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles
) {
}

