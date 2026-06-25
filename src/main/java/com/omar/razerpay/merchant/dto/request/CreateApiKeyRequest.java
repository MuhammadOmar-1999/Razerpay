package com.omar.razerpay.merchant.dto.request;

import com.omar.razerpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
