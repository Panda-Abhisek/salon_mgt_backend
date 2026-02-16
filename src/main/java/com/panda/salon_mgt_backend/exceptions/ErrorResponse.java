package com.panda.salon_mgt_backend.exceptions;

import org.springframework.http.HttpStatus;

public record ErrorResponse(String message, HttpStatus status) {
}
