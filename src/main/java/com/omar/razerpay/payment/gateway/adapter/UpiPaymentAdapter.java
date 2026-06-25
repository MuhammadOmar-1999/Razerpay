package com.omar.razerpay.payment.gateway.adapter;

import com.omar.razerpay.payment.gateway.PaymentAdapter;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentAdapter implements PaymentAdapter {
    @Override
    public void initiate(PaymentRequest request) {

    }
}
