package com.omar.razerpay.payment.service.impl;

import com.omar.razerpay.common.enums.OrderStatus;
import com.omar.razerpay.common.enums.PaymentEvent;
import com.omar.razerpay.common.enums.PaymentStatus;
import com.omar.razerpay.common.exception.BusinessRuleViolationException;
import com.omar.razerpay.common.exception.ResourceNotFoundException;
import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.PaymentInitRequest;
import com.omar.razerpay.payment.entity.OrderRecord;
import com.omar.razerpay.payment.entity.Payment;
import com.omar.razerpay.payment.gateway.PaymentGatewayRouter;
import com.omar.razerpay.payment.gateway.dto.PaymentRequest;
import com.omar.razerpay.payment.gateway.dto.PaymentResult;
import com.omar.razerpay.payment.mapper.PaymentMapper;
import com.omar.razerpay.payment.repository.OrderRepository;
import com.omar.razerpay.payment.repository.PaymentRepository;
import com.omar.razerpay.payment.service.PaymentService;
import com.omar.razerpay.payment.statemachine.PaymentTransitionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

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
        orderRepository.save(order);

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

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult paymentResult = paymentGatewayRouter.routePaymentRequest(paymentRequest);

        switch (paymentResult) {
            case PaymentResult.Pending(String registrationRef) -> payment.setProcessorReference(registrationRef);

            case PaymentResult.Failed(String errorCode, String errorDescription) -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAILURE);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
            }

            case PaymentResult.Success success -> {}
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        if(payment.getStatus() == PaymentStatus.CAPTURED) {
            throw new BusinessRuleViolationException("Payment already captured, paymentId: " + paymentId, "PAYMENT_ALREADY_CAPTURED");
        }

        if(payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessRuleViolationException("Payment can not be captured, paymentId: " + paymentId, "PAYMENT_NOT_AUTHORIZED");
        }

        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        switch (paymentResult) {
            case PaymentResult.Failed(String errorCode, String errorDescription) -> {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAILURE);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
                log.warn("Payment capture failed, Payment id: {}", payment.getId());
            }

            case PaymentResult.Success success -> {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                log.info("Payment captured successfully. Payment id: {}", payment.getId());
            }

            default -> throw new IllegalStateException("Unexpected value: " + paymentResult);
        }

        payment = paymentRepository.save(payment);
        //TODO: send an outbox (kafka event)
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    public void resolveAuthorization(UUID paymentId, boolean approved, String bankRef, String errorCode, String errorDescription) {

    }
}
