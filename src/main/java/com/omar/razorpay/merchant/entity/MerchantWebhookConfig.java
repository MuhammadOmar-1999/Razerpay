package com.omar.razorpay.merchant.entity;

import com.omar.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "merchant_webhook_config", indexes = {
        @Index(name = "idx_merchant_webhook_config_merchant_id", columnList = "merchant_id, enabled")
})
public class MerchantWebhookConfig extends BaseEntity {
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
