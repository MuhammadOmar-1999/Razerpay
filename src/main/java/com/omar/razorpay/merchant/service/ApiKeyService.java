package com.omar.razorpay.merchant.service;

import com.omar.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.omar.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.omar.razorpay.merchant.dto.response.ApiKeyResponse;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, @Valid @RequestBody CreateApiKeyRequest request);
    List<ApiKeyResponse> listByMerchant(UUID merchantId);
    void revoke(UUID merchantId, UUID id);
    public @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
