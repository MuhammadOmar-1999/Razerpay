package com.omar.razerpay.merchant.service.impl;

import com.omar.razerpay.common.enums.MerchantStatus;
import com.omar.razerpay.common.enums.UserRole;
import com.omar.razerpay.common.exception.DuplicateResourceException;
import com.omar.razerpay.merchant.dto.request.LoginRequest;
import com.omar.razerpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razerpay.merchant.dto.response.LoginResponse;
import com.omar.razerpay.merchant.dto.response.MerchantResponse;
import com.omar.razerpay.merchant.entity.AppUser;
import com.omar.razerpay.merchant.entity.Merchant;
import com.omar.razerpay.merchant.mapper.MerchantMapper;
import com.omar.razerpay.merchant.repository.AppUserRepository;
import com.omar.razerpay.merchant.repository.MerchantRepository;
import com.omar.razerpay.merchant.security.JwtUtil;
import com.omar.razerpay.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(savedMerchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(),
                            request.password())
            );
        } catch (Exception e) {
            // Catch the specific exception and return a custom 401 response
            throw new BadCredentialsException("Invalid email or password");
        }

        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateAccessToken(appUser.getEmail(),
                        appUser.getMerchant().getId(),
                        appUser.getRole().toString());

       return new LoginResponse(token);
    }
}
