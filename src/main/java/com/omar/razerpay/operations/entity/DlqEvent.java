package com.omar.razerpay.operations.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "dlq_events")
public class DlqEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "merchant_id")
    private UUID merchantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event")
    private WebhookEvent webhookEvent;

    @Column(name = "final_error", length = 1000)
    private String finalError;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @Column(name = "moved_at")
    private LocalDateTime movedAt;

    @Column(name = "replayed_at")
    private LocalDateTime replayedAt;
}
