package com.omar.razorpay.merchant.mapper;

import com.omar.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razorpay.merchant.dto.response.MerchantResponse;
import com.omar.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {
    MerchantResponse toMerchantResponse(Merchant merchant);
    Merchant toMerchantEntityFromSignupRequest(MerchantSignupRequest merchantSignupRequest);
}
