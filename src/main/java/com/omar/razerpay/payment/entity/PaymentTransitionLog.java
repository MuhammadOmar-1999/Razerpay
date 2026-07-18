package com.omar.razerpay.payment.entity;

import com.omar.razerpay.common.entity.BaseEntity;
import com.omar.razerpay.common.enums.PaymentActor;
import com.omar.razerpay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transition_log", indexes = {
        @Index(name = "idx_payment_transition_log_payment_id", columnList = "payment_id"),
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransitionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentActor actor;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

}
