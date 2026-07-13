package com.omar.razerpay.payment.gateway.adapter;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.Processor.PaymentProcessorRouter;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razerpay.payment.gateway.PaymentAdapter;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import com.omar.razerpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiating payment with UPI for paymentId: {}", request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(),
                    PaymentMethod.UPI,
                    request.amount(),
                    request.methodDetails());

            PaymentProcessorResponse paymentProcessorResponse =
                    paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failed failure ->
                        new PaymentResult.Failed(failure.errorCode(), failure.errorDescription());

                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());

                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        }
        catch (Exception e) {
            log.warn("UPI payment failed, paymentId: {}", request.paymentId(), e);
            return new PaymentResult.Failed("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_REFERENCE");
    }
}
