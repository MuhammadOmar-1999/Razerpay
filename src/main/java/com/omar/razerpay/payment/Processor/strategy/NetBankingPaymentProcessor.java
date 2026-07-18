package com.omar.razerpay.payment.Processor.strategy;

import com.omar.razerpay.common.util.RandomizerUtil;
import com.omar.razerpay.payment.Processor.PaymentProcessor;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("bank").toString() : null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failed("BANK_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef = "NBK_PROCESSOR" + RandomizerUtil.randomBase64(16);
        String redirectRef = "https://REDIRECT_BANK.COM/" + processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
