package com.omar.razorpay.merchant.dto.request;

import com.omar.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
