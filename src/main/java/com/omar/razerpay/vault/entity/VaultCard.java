package com.omar.razerpay.vault.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
public class VaultCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4, name = "last_four")
    private String lastFour;

    @Column(nullable = false, length = 6)
    private String bin;

    @Column(name = "encrypted_pan", nullable = false)
    private byte[] encryptedPan;

    @Column(name = "encrypted_dek", nullable = false)
    private byte[] encryptedDek;

    @Column(nullable = false)
    private String brand;

    @Column(name = "expiry_month", nullable = false)
    private String expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private String expiryYear;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
