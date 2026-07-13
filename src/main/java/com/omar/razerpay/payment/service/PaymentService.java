package com.omar.razerpay.payment.service;

import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.PaymentInitRequest;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request);
    PaymentResponse capture(UUID merchantId, UUID paymentId);
    void resolveAuthorization(UUID paymentId, boolean approved, String bankRef, String errorCode, String errorDescription);
}
