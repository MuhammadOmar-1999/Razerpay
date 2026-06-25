package com.omar.razerpay.payment.dto.reponse;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID merchantId,
        Money amount,
        String receipt,
        OrderStatus status,
        Integer attempts,
        Map<String, Object> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
