package com.omar.razorpay.payment.gateway;

import com.omar.razorpay.common.enums.PaymentMethod;
import com.omar.razorpay.payment.gateway.dto.PaymentRequest;
import com.omar.razorpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    public PaymentResult routePaymentRequest(PaymentRequest request){
        PaymentAdapter adapter = paymentAdapterMap.get(request.paymentMethod());
        if(adapter == null){
            throw new IllegalArgumentException("No adapter found for payment method: " + request.paymentMethod());
        }
        return adapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod paymentMethod, UUID paymentId){
        PaymentAdapter adapter = paymentAdapterMap.get(paymentMethod);
        if(adapter == null){
            throw new IllegalArgumentException("No adapter found for payment method: " + paymentMethod);
        }
        return adapter.capture(paymentId);
    }
}
