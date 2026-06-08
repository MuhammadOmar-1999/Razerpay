package com.omar.razerpay.payment.entity;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.PaymentMethod;
import com.omar.razerpay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_record_id", nullable = false)
    private OrderRecord orderRecord;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Column(nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "method_details")
    private Map<String, Object> methodDetails;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_description", length = 200)
    private String errorDescription;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;
}
