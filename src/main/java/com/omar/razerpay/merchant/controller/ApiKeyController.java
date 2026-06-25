package com.omar.razerpay.merchant.controller;

import com.omar.razerpay.merchant.dto.request.CreateApiKeyRequest;
import com.omar.razerpay.merchant.dto.response.ApiKeyCreateResponse;
import com.omar.razerpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razerpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@PathVariable UUID merchantId, @Valid @RequestBody CreateApiKeyRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.create(merchantId, request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(@PathVariable UUID merchantId){
        return ResponseEntity.status(HttpStatus.OK).body(apiKeyService.listByMerchant(merchantId));
    }

    @DeleteMapping("/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable UUID merchantId, @PathVariable UUID id){
        apiKeyService.revoke(merchantId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotate(@PathVariable UUID merchantId, @PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.rotate(merchantId, id));
    }
}

