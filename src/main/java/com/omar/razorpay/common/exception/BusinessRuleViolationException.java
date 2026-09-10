package com.omar.razorpay.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleViolationException extends RuntimeException {
    private final String errorCode;

    public BusinessRuleViolationException(String message,String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
