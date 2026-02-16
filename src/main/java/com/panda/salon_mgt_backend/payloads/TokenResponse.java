package com.panda.salon_mgt_backend.payloads;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn // this must be removed when testing is done
) {
    public static TokenResponse of(
            String accessToken,
            long expiresIn
    ) {
        return new TokenResponse(
                accessToken,
                "Bearer",
                expiresIn
        );
    }
}
