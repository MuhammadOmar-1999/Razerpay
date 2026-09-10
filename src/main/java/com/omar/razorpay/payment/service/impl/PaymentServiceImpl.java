package com.omar.razorpay.payment.service.impl;

import com.omar.razorpay.common.enums.EventAggregateType;
import com.omar.razorpay.common.enums.OrderStatus;
import com.omar.razorpay.common.enums.PaymentEvent;
import com.omar.razorpay.common.enums.PaymentStatus;
import com.omar.razorpay.common.exception.BusinessRuleViolationException;
import com.omar.razorpay.common.exception.ResourceNotFoundException;
import com.omar.razorpay.payment.dto.reponse.PaymentResponse;
import com.omar.razorpay.payment.dto.request.PaymentInitRequest;
import com.omar.razorpay.payment.entity.OrderRecord;
import com.omar.razorpay.payment.entity.Payment;
import com.omar.razorpay.payment.gateway.PaymentGatewayRouter;
import com.omar.razorpay.payment.gateway.dto.PaymentRequest;
import com.omar.razorpay.payment.gateway.dto.PaymentResult;
import com.omar.razorpay.payment.mapper.PaymentMapper;
import com.omar.razorpay.payment.outbox.OutboxEventPublisher;
import com.omar.razorpay.payment.repository.OrderRepository;
import com.omar.razorpay.payment.repository.PaymentRepository;
import com.omar.razorpay.payment.service.PaymentService;
import com.omar.razorpay.payment.statemachine.PaymentTransitionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
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
    private final OutboxEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {
//        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", request.orderId().toString()));

        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRecord", request.orderId().toString()));

        if (!(order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.PAYMENT_ATTEMPTED)) {
            throw new BusinessRuleViolationException("Cannot initiate payment as order in " + order.getStatus() + " status", "ORDER_NOT_PAYABLE");
        }

        order.setStatus(OrderStatus.PAYMENT_ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getAmount())
                .merchantId(merchantId)
                .status(PaymentStatus.CREATED)
                .method(request.paymentMethod())
                .methodDetails(request.methodDetails())
                .idempotencyKey(UUID.randomUUID().toString())
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

            case PaymentResult.Success success -> {
            }
        }

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_CREATED",
                Map.of(
                        "merchantId", merchantId.toString(),
                        "paymentId", payment.getId().toString(),
                        "orderId", order.getId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                ));

        return paymentMapper.toPaymentResponse(payment);
    }

    //the below capture method is used in the explicit capture endpoint of PaymentController
    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
//        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            throw new BusinessRuleViolationException("Payment already captured, paymentId: " + paymentId, "PAYMENT_ALREADY_CAPTURED");
        }

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
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

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_CAPTURE_ATTEMPTED",
                Map.of(
                        "merchantId", merchantId.toString(),
                        "paymentId", payment.getId().toString(),
                        "orderId", payment.getOrder().getId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                ));

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approved, String bankRef, String errorCode, String errorDescription) {
//        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
//                () -> new ResourceNotFoundException("Payment", paymentId.toString())
//        );

        Payment payment = paymentRepository.findByIdForUpdate(paymentId).stream().findFirst().orElseThrow(
                () -> new ResourceNotFoundException("Payment", paymentId.toString())
        );

        if (payment.getStatus() != PaymentStatus.AUTHORIZING) {
            log.warn("Payment is not in AUTHORIZING state, paymentId: {}, currentStatus: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();
        if (approved) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            //Auto-capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if (captureResult instanceof PaymentResult.Success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setStatus(OrderStatus.PAID);
                log.info("Payment captured successfully. Payment id: {}", payment.getId());
            } else {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAILURE);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
            }
        }
        else{
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAILURE);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_AUTHORIZATION_RESOLVED",
                Map.of(
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentId", payment.getId().toString(),
                        "orderId", payment.getOrder().getId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()
                ));

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
    }
}
