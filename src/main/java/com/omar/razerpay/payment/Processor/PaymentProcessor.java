package com.omar.razerpay.payment.Processor;

import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {
    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
