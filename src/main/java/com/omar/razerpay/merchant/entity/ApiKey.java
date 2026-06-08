package com.omar.razerpay.merchant.entity;

import com.omar.razerpay.common.enums.Environment;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_key")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 50, unique = true)
    private String keyId;

    @Column(nullable = false, length = 255)
    private String keySecretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Environment environment;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime lastUsedAt;
    private LocalDateTime rotatedAt;
    private LocalDateTime gracePeriodExpiresAt;

}
