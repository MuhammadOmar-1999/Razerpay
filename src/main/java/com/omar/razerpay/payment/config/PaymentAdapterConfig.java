package com.omar.razerpay.payment.config;

import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.payment.gateway.PaymentAdapter;
import com.omar.razerpay.payment.gateway.adapter.CardPaymentAdapter;
import com.omar.razerpay.payment.gateway.adapter.NetBankingAdapter;
import com.omar.razerpay.payment.gateway.adapter.UpiPaymentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentAdapterConfig {
    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentAdapter(),
                PaymentMethod.NET_BANKING, new NetBankingAdapter(),
                PaymentMethod.UPI, new UpiPaymentAdapter()
        );
    }
}
