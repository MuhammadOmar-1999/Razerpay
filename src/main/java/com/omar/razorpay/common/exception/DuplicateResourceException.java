package com.omar.razorpay.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String errorCode;
    private final String resourceName;
    private final String identifier;

    public DuplicateResourceException(String errorCode,  String resourceName, String identifier) {
        super(String.format("%s '%s' already exists", resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
        this.errorCode = errorCode;
    }
}
