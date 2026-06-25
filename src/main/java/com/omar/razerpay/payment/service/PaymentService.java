package com.omar.razerpay.payment.service;

import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.PaymentInitRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request);
}
