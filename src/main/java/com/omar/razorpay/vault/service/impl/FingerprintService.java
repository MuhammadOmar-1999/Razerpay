package com.omar.razorpay.vault.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class FingerprintService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${vault.fingerprint.secret}")
    private String fingerprintSecret;

    /**
     * Generates a deterministic, irreversible HMAC-SHA256 hash of the PAN.
     */
    public String generatePanFingerprint(String pan) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    fingerprintSecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(pan.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to a standard Hex string (Requires Java 17+)
            // For older Java versions, use Apache Commons Codec: Hex.encodeHexString(hashBytes)
            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            // In a real application, wrap this in a custom CryptoException
            throw new RuntimeException("Failed to generate PAN fingerprint", e);
        }
    }
}