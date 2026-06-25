package com.omar.razerpay.payment.service.impl;

import com.omar.razerpay.common.enums.OrderStatus;
import com.omar.razerpay.common.enums.PaymentStatus;
import com.omar.razerpay.common.exception.BusinessRuleViolationException;
import com.omar.razerpay.common.exception.ResourceNotFoundException;
import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.PaymentInitRequest;
import com.omar.razerpay.payment.entity.OrderRecord;
import com.omar.razerpay.payment.entity.Payment;
import com.omar.razerpay.payment.gateway.PaymentGatewayRouter;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import com.omar.razerpay.payment.repository.OrderRepository;
import com.omar.razerpay.payment.repository.PaymentRepository;
import com.omar.razerpay.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", request.orderId().toString()));

        if (!(order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.ATTEMPTED)) {
            throw new BusinessRuleViolationException("Cannot initiate payment as order in " + order.getStatus() + "status", "ORDER_NOT_PAYABLE");
        }

        order.setStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getAmount())
                .merchantId(merchantId)
                .status(PaymentStatus.CREATED)
                .method(request.paymentMethod())
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),
                request.orderId(),
                merchantId,
                payment.getAmount(),
                payment.getMethod(),
                payment.getMethodDetails()
        );

        paymentGatewayRouter.routePaymentRequest(paymentRequest);
        return null;
    }
}
