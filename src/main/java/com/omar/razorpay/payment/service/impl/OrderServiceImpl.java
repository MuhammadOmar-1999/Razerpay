package com.omar.razorpay.payment.service.impl;

import com.omar.razorpay.common.enums.EventAggregateType;
import com.omar.razorpay.common.enums.OrderStatus;
import com.omar.razorpay.common.exception.BusinessRuleViolationException;
import com.omar.razorpay.common.exception.DuplicateResourceException;
import com.omar.razorpay.common.exception.ResourceNotFoundException;
import com.omar.razorpay.merchant.service.CustomerService;
import com.omar.razorpay.payment.dto.reponse.OrderResponse;
import com.omar.razorpay.payment.dto.reponse.PaymentResponse;
import com.omar.razorpay.payment.dto.request.CreateOrderRequest;
import com.omar.razorpay.payment.entity.OrderRecord;
import com.omar.razorpay.payment.mapper.OrderMapper;
import com.omar.razorpay.payment.mapper.PaymentMapper;
import com.omar.razorpay.payment.outbox.OutboxEventPublisher;
import com.omar.razorpay.payment.repository.OrderRepository;
import com.omar.razorpay.payment.repository.PaymentRepository;
import com.omar.razorpay.payment.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final OutboxEventPublisher eventPublisher;

    @Value("${payment.order.default-order-expiry-minute:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request)  {
        if(request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt()))
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Receipt", request.receipt());

        LocalDateTime expiresAt = request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes);


        UUID customerId = null;
        if(request.customer() != null){
            customerId = customerService.findOrCreate(
                    merchantId,
                    request.customer().name(),
                    request.customer().email(),
                    request.customer().contactNumber());
        }

        OrderRecord orderRecord = OrderRecord.builder()
                .merchantId(merchantId)
                .customerId(customerId)
                .amount(request.amount())
                .receipt(request.receipt())
                .notes(request.notes())
                .expiresAt(expiresAt)
                .build();

        orderRecord = orderRepository.save(orderRecord);

        eventPublisher.publish(EventAggregateType.ORDER, orderRecord.getId(), "ORDER_CREATED",
                Map.of(
                "merchantId", merchantId.toString(),
                "orderId", orderRecord.getId().toString(),
                "orderStatus", orderRecord.getStatus().name(),
                "amountUnits", request.amount().getAmountUnits(),
                "amountCurrency", request.amount().getCurrency(),
                "receipt", request.receipt() !=null ? request.receipt() : "",
                "expiresAt", expiresAt.toString()
        ));

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

    eventPublisher.publish(EventAggregateType.ORDER, orderRecord.getId(), "ORDER_CANCELLED",
                Map.of(
                        "merchantId", merchantId.toString(),
                        "orderId", orderRecord.getId().toString(),
                        "orderStatus", orderRecord.getStatus().name(),
                        "amountUnits", orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency", orderRecord.getAmount().getCurrency(),
                        "receipt", orderRecord.getReceipt() !=null ? orderRecord.getReceipt() : ""
                ));

        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", orderId.toString()));

        return paymentMapper.toPaymentResponseList(paymentRepository.findByOrderId(orderId));
    }
}
