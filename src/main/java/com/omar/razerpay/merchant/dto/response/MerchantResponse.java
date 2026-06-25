package com.omar.razerpay.merchant.dto.response;

import com.omar.razerpay.common.enums.BusinessType;
import com.omar.razerpay.common.enums.MerchantStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public record MerchantResponse(
    UUID id,
    String name,
    String email,
    String businessName,
    BusinessType businessType,
    MerchantStatus status
) {
}
