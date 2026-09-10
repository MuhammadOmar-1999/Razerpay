package com.omar.razorpay.vault.entity;

import com.omar.razorpay.common.entity.BaseEntity;
import com.omar.razorpay.common.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaultCard extends BaseEntity {
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
    private String fingerprint;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardBrand brand;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
