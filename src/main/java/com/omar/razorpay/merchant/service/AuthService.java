package com.omar.razorpay.merchant.service;

import com.omar.razorpay.merchant.dto.request.LoginRequest;
import com.omar.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.omar.razorpay.merchant.dto.response.LoginResponse;
import com.omar.razorpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
    LoginResponse login(LoginRequest request);
}
