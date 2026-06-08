package com.omar.razerpay.payment.entity;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_record")
public class OrderRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // no FK - cross-service boundary
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    private Integer attempts;

    @JdbcTypeCode((SqlTypes.JSON))
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
