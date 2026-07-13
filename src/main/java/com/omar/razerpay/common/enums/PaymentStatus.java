package com.omar.razerpay.common.enums;

public enum PaymentStatus {
    CREATED,
    AUTHORIZING,
    AUTHORIZED,
    CAPTURING,
    CAPTURED,
    FAILED,
    CANCELLED,
    REFUNDED,
    REFUNDING,
    PARTIALLY_REFUNDED,
    SETTLED,
    AUTH_EXPIRED
}
