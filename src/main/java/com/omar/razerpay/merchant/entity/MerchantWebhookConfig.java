package com.omar.razerpay.merchant.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "merchant_webhook_config")
public class MerchantWebhookConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(name = "event_type_filter")
    private String eventTypeFilter;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "webhook_secret_hash", length = 300)
    private String webhoookSecretHash;

    @Column(length = 300)
    private String eventTypes;
    //Comma seperated list of event types to subscribe to
}
