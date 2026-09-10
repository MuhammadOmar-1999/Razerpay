package com.omar.razorpay.operations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SettlementPaymentId implements Serializable {
    @Column(name = "settlement_id", nullable = false)
    private UUID settlementId;
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;
}
