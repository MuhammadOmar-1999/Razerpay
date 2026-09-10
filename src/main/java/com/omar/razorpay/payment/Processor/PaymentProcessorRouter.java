package com.omar.razorpay.payment.Processor;

import com.omar.razorpay.common.enums.PaymentMethod;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {
    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessorMap.get(request.paymentMethod());
        if(processor == null){
            throw new IllegalArgumentException("No payment processor found for payment method "+request.paymentMethod());
        }
        return processor.charge(request);
    }
}
