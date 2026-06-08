package com.omar.razerpay.vault.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_token")
public class CardToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    private VaultCard card;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    private LocalDateTime revokedAt;
}
