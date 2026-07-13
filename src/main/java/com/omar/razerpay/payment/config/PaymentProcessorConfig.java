package com.omar.razerpay.payment.config;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.Processor.PaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.CardPaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.NetBankingPaymentProcessor;
import com.omar.razerpay.payment.Processor.strategy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.NET_BANKING, new NetBankingPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor()
        );
    }
}
