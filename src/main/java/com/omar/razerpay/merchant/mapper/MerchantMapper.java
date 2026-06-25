package com.omar.razerpay.merchant.mapper;

import com.omar.razerpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razerpay.merchant.dto.response.MerchantResponse;
import com.omar.razerpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {
    MerchantResponse toMerchantResponse(Merchant merchant);
    Merchant toMerchantEntityFromSignupRequest(MerchantSignupRequest merchantSignupRequest);
}
