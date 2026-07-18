package com.omar.razerpay.merchant.service;

import com.omar.razerpay.merchant.dto.request.LoginRequest;
import com.omar.razerpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razerpay.merchant.dto.response.LoginResponse;
import com.omar.razerpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
    LoginResponse login(LoginRequest request);
}
