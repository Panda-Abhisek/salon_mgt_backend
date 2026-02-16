package com.panda.salon_mgt_backend.payloads;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServiceCreateRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull
        @Min(5)
        @Max(600)
        Integer durationMinutes

) {}
