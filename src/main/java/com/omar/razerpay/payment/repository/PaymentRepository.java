package com.omar.razerpay.payment.repository;

import com.omar.razerpay.payment.entity.OrderRecord;
import com.omar.razerpay.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
}
