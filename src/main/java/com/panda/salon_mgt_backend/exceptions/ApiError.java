package com.panda.salon_mgt_backend.exceptions;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiError of(HttpStatus status, String upgradeRequired, String s, String requestURI) {
        return new ApiError(Instant.now(), status.value(), upgradeRequired, s, requestURI);
    }
}