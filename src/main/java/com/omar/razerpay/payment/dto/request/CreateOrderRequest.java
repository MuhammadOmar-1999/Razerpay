package com.omar.razerpay.payment.dto.request;

import com.omar.razerpay.common.entity.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(
        @NotNull(message = "Amount is required")
        Money amount,

        @Size(max = 100, message = "Currency should not be more than 100 characters long")
        String receipt,

        Map<String, Object> notes,
        LocalDateTime expiresAt
) {
}
