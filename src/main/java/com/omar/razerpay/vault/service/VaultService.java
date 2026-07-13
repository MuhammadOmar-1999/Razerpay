package com.omar.razerpay.vault.service;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razerpay.vault.dto.request.TokenizeRequest;
import com.omar.razerpay.vault.dto.response.TokenizeResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId);
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
