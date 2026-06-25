package com.omar.razerpay.payment.controller;

import com.omar.razerpay.payment.dto.reponse.PaymentResponse;
import com.omar.razerpay.payment.dto.request.PaymentInitRequest;
import com.omar.razerpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    UUID merchantId = UUID.fromString("d1e2a800-6e70-47df-9b09-d16ac53ada33"); //TODO: replace it with MerchantContext

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantId, request));
    }

}
