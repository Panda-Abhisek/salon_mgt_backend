package com.panda.salon_mgt_backend.exceptions;

public class InactiveException extends RuntimeException {
    public InactiveException(String message) {
        super(message);
    }
}
