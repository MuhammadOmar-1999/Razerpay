package com.omar.razorpay.payment.service;

import com.omar.razorpay.payment.dto.reponse.OrderResponse;
import com.omar.razorpay.payment.dto.reponse.PaymentResponse;
import com.omar.razorpay.payment.dto.request.CreateOrderRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest request);
    OrderResponse getById(UUID merchantId, UUID orderId);
    OrderResponse cancel(UUID merchantId, UUID orderId);
    List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}
