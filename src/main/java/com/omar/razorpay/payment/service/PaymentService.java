package com.omar.razorpay.payment.service;

import com.omar.razorpay.payment.dto.reponse.PaymentResponse;
import com.omar.razorpay.payment.dto.request.PaymentInitRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request);
    PaymentResponse capture(UUID merchantId, UUID paymentId);
    void resolveAuthorization(UUID paymentId, boolean approved, String bankRef, String errorCode, String errorDescription);
}
