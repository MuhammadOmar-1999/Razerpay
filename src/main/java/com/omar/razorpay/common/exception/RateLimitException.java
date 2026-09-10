package com.omar.razorpay.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitException extends RuntimeException {
   private Integer retryAfterSeconds;
    public RateLimitException(String message, Integer retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
