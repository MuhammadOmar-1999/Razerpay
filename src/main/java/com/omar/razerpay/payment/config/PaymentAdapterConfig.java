package com.omar.razerpay.payment.config;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.Processor.PaymentProcessorRouter;
import com.omar.razerpay.payment.gateway.PaymentAdapter;
import com.omar.razerpay.payment.gateway.adapter.CardPaymentAdapter;
import com.omar.razerpay.payment.gateway.adapter.NetBankingAdapter;
import com.omar.razerpay.payment.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final CardPaymentAdapter cardPaymentAdapter;
    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NET_BANKING, netBankingAdapter,
                PaymentMethod.UPI, upiPaymentAdapter
        );
    }
}
