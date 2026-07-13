package com.omar.razerpay.payment.repository;

import com.omar.razerpay.common.enums.PaymentStatus;
import com.omar.razerpay.payment.entity.OrderRecord;
import com.omar.razerpay.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);
}
