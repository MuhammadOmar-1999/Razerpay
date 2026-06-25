package com.omar.razerpay.merchant.service.impl;

import com.omar.razerpay.common.enums.MerchantStatus;
import com.omar.razerpay.common.enums.UserRole;
import com.omar.razerpay.common.exception.DuplicateResourceException;
import com.omar.razerpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razerpay.merchant.dto.response.MerchantResponse;
import com.omar.razerpay.merchant.entity.AppUser;
import com.omar.razerpay.merchant.entity.Merchant;
import com.omar.razerpay.merchant.mapper.MerchantMapper;
import com.omar.razerpay.merchant.repository.AppUserRepository;
import com.omar.razerpay.merchant.repository.MerchantRepository;
import com.omar.razerpay.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "MerchantEmail",  request.email());
        }

        Merchant merchant = merchantMapper.toMerchantEntityFromSignupRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);

        Merchant savedMerchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .merchant(savedMerchant)
                .email(merchant.getEmail())
                .role(UserRole.OWNER)
                .passwordHash(request.password()) //TODO: encrypt using BcryptPasswordEncoder
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(savedMerchant);
    }
}
