package com.omar.razerpay.payment.gateway;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    public void routePaymentRequest(PaymentRequest request){
        PaymentAdapter adapter = paymentAdapterMap.get(request.paymentMethod());
        if(adapter == null){
            throw new IllegalArgumentException("No adapter found for payment method: " + request.paymentMethod());
        }
        adapter.initiate(request);
    }
}
