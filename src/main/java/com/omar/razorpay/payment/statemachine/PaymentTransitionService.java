package com.omar.razorpay.payment.statemachine;

import com.omar.razorpay.common.enums.PaymentActor;
import com.omar.razorpay.common.enums.PaymentEvent;
import com.omar.razorpay.common.enums.PaymentStatus;
import com.omar.razorpay.payment.entity.Payment;
import com.omar.razorpay.payment.entity.PaymentTransitionLog;
import com.omar.razorpay.payment.repository.PaymentTransitionLogRepository;
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
