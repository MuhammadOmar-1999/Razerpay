package com.omar.razorpay.payment.Processor;

import com.omar.razorpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {
    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
