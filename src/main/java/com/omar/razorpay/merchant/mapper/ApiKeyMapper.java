package com.omar.razorpay.merchant.mapper;

import com.omar.razorpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razorpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {
    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);
}

