package com.omar.razerpay.merchant.repository;

import com.omar.razerpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razerpay.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId);
}
