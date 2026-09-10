package com.omar.razorpay.vault.service;

import com.omar.razorpay.common.entity.Money;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razorpay.vault.dto.request.TokenizeRequest;
import com.omar.razorpay.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId);
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
