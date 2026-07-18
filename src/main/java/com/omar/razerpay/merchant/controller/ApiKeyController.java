package com.omar.razerpay.merchant.controller;

import com.omar.razerpay.merchant.dto.request.CreateApiKeyRequest;
import com.omar.razerpay.merchant.dto.response.ApiKeyCreateResponse;
import com.omar.razerpay.merchant.dto.response.ApiKeyResponse;
import com.omar.razerpay.merchant.security.MerchantContext;
import com.omar.razerpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@Valid @RequestBody CreateApiKeyRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(){
        return ResponseEntity.status(HttpStatus.OK).body(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable String id){
        apiKeyService.revoke(merchantContext.getMerchantId(), UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotate(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.rotate(merchantContext.getMerchantId(),
                UUID.fromString(id)));
    }
}

