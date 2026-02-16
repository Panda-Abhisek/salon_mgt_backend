package com.panda.salon_mgt_backend.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StaffCreateRequest(
        @NotBlank String name,
        @Email String email,
        @NotBlank String password
) {}
