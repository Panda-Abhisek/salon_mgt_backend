package com.panda.salon_mgt_backend.security.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
