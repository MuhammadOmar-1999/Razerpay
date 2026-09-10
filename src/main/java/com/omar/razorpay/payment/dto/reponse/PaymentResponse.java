package com.omar.razorpay.payment.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omar.razorpay.common.entity.Money;
import com.omar.razorpay.common.enums.PaymentMethod;
import com.omar.razorpay.common.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentStatus status,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        String bankReference,
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt
) {

}
