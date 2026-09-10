package com.omar.razorpay.payment.gateway.dto;

import com.omar.razorpay.common.entity.Money;
import com.omar.razorpay.common.enums.PaymentMethod;

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
