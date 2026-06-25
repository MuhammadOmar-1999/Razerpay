package com.omar.razerpay.payment.gateway.dto;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails
) {

}
