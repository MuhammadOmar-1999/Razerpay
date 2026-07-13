package com.omar.razerpay.payment.gateway.adapter;

import com.omar.razerpay.payment.Processor.PaymentProcessorRouter;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razerpay.payment.gateway.PaymentAdapter;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import com.omar.razerpay.payment.gateway.dto.PaymentResult;
import com.omar.razerpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;


    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");
        PaymentProcessorResponse paymentProcessorResponse = vaultService.charge(request.paymentId(), token, request.amount(), request.methodDetails());

        return switch (paymentProcessorResponse) {
            case PaymentProcessorResponse.Failed failure ->
                    new PaymentResult.Failed(failure.errorCode(), failure.errorDescription());

            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());

            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
