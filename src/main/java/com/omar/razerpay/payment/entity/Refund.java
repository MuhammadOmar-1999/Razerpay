package com.omar.razerpay.payment.entity;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.RefundStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.SQLType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "Refund")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Embedded
    private Money money;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(length = 100, name = "error_code")
    private String errorCode;

    @Column(length = 500, name = "error_description")
    private String errorDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
