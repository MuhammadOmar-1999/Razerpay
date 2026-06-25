package com.omar.razerpay.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String errorCode,
        String errorDescription,
        LocalDateTime timeStamp,
        List<FieldError> fieldError
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(String errorCode, String errorDescription) {
        return new ErrorResponse(errorCode, errorDescription, LocalDateTime.now(), null);
    }

    public static ErrorResponse of(String errorCode, String errorDescription, List<FieldError> fieldError) {
        return new ErrorResponse(errorCode, errorDescription, LocalDateTime.now(), fieldError);
    }
}
