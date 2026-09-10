package com.omar.razorpay.payment.controller;

import com.omar.razorpay.merchant.security.MerchantContext;
import com.omar.razorpay.payment.dto.reponse.PaymentResponse;
import com.omar.razorpay.payment.dto.request.PaymentInitRequest;
import com.omar.razorpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantContext.getMerchantId(), request));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable("paymentId") UUID paymentId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}


