package com.omar.razerpay.payment.Processor.strategy;

import com.omar.razerpay.common.util.RandomizerUtil;
import com.omar.razerpay.payment.Processor.PaymentProcessor;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("BANK").toString() : null;

        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failed("UPI_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
