package com.omar.razerpay.merchant.dto.response;

import com.omar.razerpay.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment
        ) {
}
