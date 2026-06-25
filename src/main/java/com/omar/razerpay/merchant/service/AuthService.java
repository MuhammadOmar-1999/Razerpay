package com.omar.razerpay.merchant.service;

import com.omar.razerpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razerpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
}
