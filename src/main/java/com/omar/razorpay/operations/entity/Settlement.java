package com.omar.razorpay.operations.entity;

import com.omar.razorpay.common.entity.BaseEntity;
import com.omar.razorpay.common.entity.Money;
import com.omar.razorpay.common.enums.SettlementStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
public class Settlement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "gross_amount_currency"))
    })
    private Money grossAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "refund_amount_currency"))
    })
    private Money refundAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gst_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "gst_amount_currency"))
    })
    private Money gstAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_amount_currency"))
    })
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "net_amount_currency"))
    })
    private Money netAmount;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;
}
