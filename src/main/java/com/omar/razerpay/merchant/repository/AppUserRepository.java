package com.omar.razerpay.merchant.repository;

import com.omar.razerpay.merchant.entity.AppUser;
import com.omar.razerpay.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
}
