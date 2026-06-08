package com.omar.razerpay.payment.entity;

import com.omar.razerpay.common.enums.PaymentActor;
import com.omar.razerpay.common.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentTransitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private PaymentStatus fromStatus;

    @Column(name = "to_status", length = 20)
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentActor actor;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

}
