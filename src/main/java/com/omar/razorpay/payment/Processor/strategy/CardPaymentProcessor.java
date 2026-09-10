package com.omar.razorpay.payment.Processor.strategy;

import com.omar.razorpay.common.util.RandomizerUtil;
import com.omar.razorpay.payment.Processor.PaymentProcessor;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000001";
    public static final String PAN_CARD_EXPIRED = "4000000000000002";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        String pan = request.pan();
        if(PAN_CARD_DECLINED.equals(pan)) {
            log.warn("Card declined");
            return new PaymentProcessorResponse.Failed("CARD_DECLINED", "Card declined");
        }
        if(PAN_CARD_EXPIRED.equals(pan)) {
            log.warn("Card expired");
            return new PaymentProcessorResponse.Failed("CARD_EXPIRED", "Card expired");
        }

        String processorRef = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
