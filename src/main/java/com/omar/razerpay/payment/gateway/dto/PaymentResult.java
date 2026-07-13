package com.omar.razerpay.payment.gateway.dto;

public sealed interface PaymentResult {
    record Pending(String registrationReference) implements PaymentResult {}
    record Failed(String errorCode, String errorDescription) implements PaymentResult {}
    record Success(String bankReference) implements PaymentResult {}
}
