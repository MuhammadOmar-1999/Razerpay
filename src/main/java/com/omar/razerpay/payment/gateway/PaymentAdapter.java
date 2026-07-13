package com.omar.razerpay.payment.gateway;

import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import com.omar.razerpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {
    PaymentResult initiate(PaymentRequest paymentRequest);
    PaymentResult capture(UUID paymentId);
}
