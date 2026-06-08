package com.omar.razerpay.merchant.entity;


import com.omar.razerpay.common.enums.BusinessType;
import com.omar.razerpay.common.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "merchant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MerchantStatus status =  MerchantStatus.PENDING_KYC;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BusinessType  businessType;

    @Column(length = 200)
    private String websiteUrl;

    @Column(length = 20)
    private String gstId;

    @Column(length = 20)
    private String panId;

    @Column(length = 200)
    private String settlementBankAccount;

    @Column(length = 20)
    private String settlementBankIfsc;

    @Column(length = 100)
    private String settlementBankAccountHolderName;

}
