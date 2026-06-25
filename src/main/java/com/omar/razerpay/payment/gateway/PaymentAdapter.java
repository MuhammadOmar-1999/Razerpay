package com.omar.razerpay.payment.gateway;

import com.omar.razerpay.payment.gateway.dto.PaymentRequest;

public interface PaymentAdapter {
    void initiate(PaymentRequest paymentRequest);
}
