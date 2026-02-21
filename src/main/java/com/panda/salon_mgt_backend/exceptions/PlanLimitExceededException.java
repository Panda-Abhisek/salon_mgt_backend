package com.panda.salon_mgt_backend.exceptions;

import lombok.Getter;

@Getter
public class PlanLimitExceededException extends RuntimeException {

    private final int limit;
    private final String code = "PLAN_LIMIT_EXCEEDED";
    private final boolean upgradeRequired = true;

    public PlanLimitExceededException(String message, int limit) {
        super(message);
        this.limit = limit;
    }
}
