package com.omar.razorpay.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class VaultEncryptionConfig {

    @Value("${vault.master-key}")
    private String masterKey;

    public static BytesEncryptor panEncryptor(byte[] dek){
        SecretKeySpec decKey = getAesDek(dek);
        return new AesBytesEncryptor(decKey, KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    public BytesEncryptor dekEncryptor(){
        SecretKeySpec decKey = getAesDek(masterKey);
        return new AesBytesEncryptor(decKey, KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    // 1. Strict Byte Array Method (Validates length)
    private static SecretKeySpec getAesDek(byte[] keyBytes) {
        if (keyBytes == null || (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32)) {
            throw new IllegalArgumentException("Invalid AES key length.");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    // 2. Strict String Method (Decodes, then calls the byte array method)
    private static SecretKeySpec getAesDek(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException("Key string cannot be empty.");
        }
        return getAesDek(Base64.getDecoder().decode(base64Key));
    }
}
