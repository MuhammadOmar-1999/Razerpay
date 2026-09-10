package com.omar.razorpay.merchant.service;

import java.util.UUID;

public interface CustomerService {
    UUID findOrCreate(UUID merchantId, String name, String email, String contactNumber);
}
