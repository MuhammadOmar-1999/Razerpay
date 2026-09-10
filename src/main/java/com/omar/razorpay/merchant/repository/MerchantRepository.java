package com.omar.razorpay.merchant.repository;

import com.omar.razorpay.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByEmail(String email);
    Optional<Merchant> findByEmail(String email);
}
