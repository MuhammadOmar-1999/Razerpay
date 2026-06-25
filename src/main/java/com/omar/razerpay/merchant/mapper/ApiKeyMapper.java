package com.omar.razerpay.merchant.mapper;

import com.omar.razerpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razerpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {
    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);
}

