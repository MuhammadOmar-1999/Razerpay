package com.omar.razerpay.vault.dto.response;

import com.omar.razerpay.common.enums.CardBrand;
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
