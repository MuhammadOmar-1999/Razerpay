package com.omar.razorpay.merchant.service.impl;

import com.omar.razorpay.common.exception.ResourceNotFoundException;
import com.omar.razorpay.common.util.RandomizerUtil;
import com.omar.razorpay.merchant.cache.ApiKeyCache;
import com.omar.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.omar.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.omar.razorpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razorpay.merchant.entity.ApiKey;
import com.omar.razorpay.merchant.entity.Merchant;
import com.omar.razorpay.merchant.mapper.ApiKeyMapper;
import com.omar.razorpay.merchant.repository.ApiKeyRepository;
import com.omar.razorpay.merchant.repository.MerchantRepository;
import com.omar.razorpay.merchant.service.ApiKeyService;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
         Merchant merchant = merchantRepository.findById(merchantId)
                 .orElseThrow(
                         ()->new ResourceNotFoundException("merchant", merchantId.toString())
                 );

         String rawSecret = RandomizerUtil.randomBase64(40);
         String keyId = "rzp_" + request.environment().name().toLowerCase() + RandomizerUtil.randomBase64(24);
         ApiKey apiKey = ApiKey.builder()
                 .merchant(merchant)
                 .keyId(keyId)
                 .keySecretHash(passwordEncoder.encode(rawSecret))
                 .environment(request.environment())
                 .build();

         apiKey = apiKeyRepository.save(apiKey);

         return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.environment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchant_Id(merchantId).stream().map(
                apiKeyMapper::toApiKeyResponse
        ).toList();
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID id) {
        ApiKey key = apiKeyRepository.findById(id)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()-> new ResourceNotFoundException("ApiKeyId", id.toString()));

        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());
    }

    @Override
    @Transactional
    public @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKeyId", id.toString()));

        if(!apiKey.isEnabled()) {
            throw new RuntimeException("Api key is disabled, cannot be rotated");
        }

        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(passwordEncoder.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        apiKey = apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());

        return new ApiKeyCreateResponse(
                apiKey.getId(),
                apiKey.getKeyId(),
                newRawSecret,
                apiKey.getEnvironment());
    }
}
