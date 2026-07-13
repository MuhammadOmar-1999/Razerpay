package com.omar.razerpay.vault.controller;

import com.omar.razerpay.vault.dto.request.TokenizeRequest;
import com.omar.razerpay.vault.dto.response.TokenizeResponse;
import com.omar.razerpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    UUID merchantId = UUID.fromString("d1e2a800-6e70-47df-9b09-d16ac53ada33"); //TODO: replace it with MerchantContext

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest tokenizeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenize(tokenizeRequest, merchantId));
    }

}

