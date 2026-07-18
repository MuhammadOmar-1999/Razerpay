package com.omar.razerpay.payment.service.impl;

import com.omar.razerpay.common.enums.OrderStatus;
import com.omar.razerpay.common.exception.BusinessRuleViolationException;
import com.omar.razerpay.common.exception.DuplicateResourceException;
import com.omar.razerpay.common.exception.ResourceNotFoundException;
import com.omar.razerpay.payment.dto.reponse.OrderResponse;
import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.CreateOrderRequest;
import com.omar.razerpay.payment.entity.OrderRecord;
import com.omar.razerpay.payment.mapper.OrderMapper;
import com.omar.razerpay.payment.mapper.PaymentMapper;
import com.omar.razerpay.payment.repository.OrderRepository;
import com.omar.razerpay.payment.repository.PaymentRepository;
import com.omar.razerpay.payment.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${payment.order.default-order-expiry-minute:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt()))
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Receipt", request.receipt());

        LocalDateTime expiresAt = request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes);

        OrderRecord orderRecord = OrderRecord.builder()
                .merchantId(merchantId)
                .amount(request.amount())
                .receipt(request.receipt())
                .notes(request.notes())
                .expiresAt(expiresAt)
                .build();

        orderRecord = orderRepository.save(orderRecord);


        //TODO: publish send kafka event about order creation
        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    @Transactional
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        return orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", orderId.toString()));
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord orderRecord = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", orderId.toString()));

        if (orderRecord.getStatus() == OrderStatus.CANCELLED || orderRecord.getStatus() == OrderStatus.PAID) {
            throw new BusinessRuleViolationException("Order cannot be cancelled as it is already " + orderRecord.getStatus(), "ORDER_CANNOT_BE_CANCELLED");
        }

        orderRecord.setStatus(OrderStatus.CANCELLED);
        orderRecord = orderRepository.save(orderRecord);

        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", orderId.toString()));

        return paymentMapper.toPaymentResponseList(paymentRepository.findByOrderId(orderId));
    }
}
