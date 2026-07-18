package com.omar.razerpay.payment.config;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.Processor.PaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.CardPaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.NetBankingPaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NET_BANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
