package com.omar.razerpay.payment.statemachine;

import com.omar.razerpay.common.enums.PaymentActor;
import com.omar.razerpay.common.enums.PaymentEvent;
import com.omar.razerpay.common.enums.PaymentStatus;
import com.omar.razerpay.payment.entity.Payment;
import com.omar.razerpay.payment.entity.PaymentTransitionLog;
import com.omar.razerpay.payment.repository.PaymentTransitionLogRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PaymentTransitionService {
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    @Transactional
    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), paymentEvent);


        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO fetch actor of security context
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(next);
        paymentTransitionLogRepository.save(paymentTransitionLog);
        return next;
    }
}
