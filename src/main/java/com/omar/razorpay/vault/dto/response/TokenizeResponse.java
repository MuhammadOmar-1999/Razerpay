package com.omar.razorpay.vault.dto.response;

import com.omar.razorpay.common.enums.CardBrand;
import lombok.Builder;

@Builder
public record TokenizeResponse(
        String token,
        String lastFour,
        String bin,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
