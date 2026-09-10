package com.omar.razorpay.vault.service.impl;

import com.omar.razorpay.common.entity.Money;
import com.omar.razorpay.common.enums.CardBrand;
import com.omar.razorpay.common.util.RandomizerUtil;
import com.omar.razorpay.payment.Processor.PaymentProcessorRouter;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razorpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razorpay.vault.config.VaultEncryptionConfig;
import com.omar.razorpay.vault.dto.request.TokenizeRequest;
import com.omar.razorpay.vault.dto.response.TokenizeResponse;
import com.omar.razorpay.vault.entity.CardToken;
import com.omar.razorpay.vault.entity.VaultCard;
import com.omar.razorpay.vault.repository.CardTokenRepository;
import com.omar.razorpay.vault.repository.VaultCardRepository;
import com.omar.razorpay.vault.service.VaultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;
    private final FingerprintService fingerprintService;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String pan = request.pan();

        // 1. Generate a deterministic cryptographic fingerprint (e.g., HMAC-SHA256)
        // Note: This must use a highly secure, static, gateway-wide secret key (pepper).
        String fingerprint = fingerprintService.generatePanFingerprint(pan);

        // 2. Check if the underlying card already exists in the Vault
        VaultCard vaultCard = vaultCardRepository.findByFingerprint(fingerprint)
                .orElseGet(() -> createAndSaveNewVaultCard(request, fingerprint));

        // 3. Check if THIS merchant already has a token mapped to THIS vaulted card
        CardToken cardToken = cardTokenRepository.findByVaultCardAndMerchantId(vaultCard, merchantId)
                .orElseGet(() -> createAndSaveNewToken(vaultCard, merchantId, request.customerId()));

        // 4. Return the response using the unified data
        return TokenizeResponse.builder()
                .token(cardToken.getToken())
                .lastFour(vaultCard.getLastFour())
                .bin(vaultCard.getBin())
                .brand(vaultCard.getBrand())
                .expiryMonth(vaultCard.getExpiryMonth())
                .expiryYear(vaultCard.getExpiryYear())
                .build();
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or revoked token"));

        VaultCard card = cardToken.getVaultCard();
        byte[] decryptedPan = null;

        try {
            byte[] decryptedDek = dekEncryptor.decrypt(card.getEncryptedDek());
            decryptedPan = VaultEncryptionConfig.panEncryptor(decryptedDek)
                    .decrypt(card.getEncryptedPan());

            String pan = new String(decryptedPan, StandardCharsets.UTF_8);
            String expiry = card.getExpiryMonth() + "/" + card.getExpiryYear();
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(paymentId, pan, expiry, amount, methodDetails);
            log.info("Vault charge registered, token {}*********", token.substring(0, 4));

            return paymentProcessorRouter.charge(paymentProcessorRequest);

        } catch (Exception e) {
            log.warn("Vault charge failed, token: {}", token);
            return new PaymentProcessorResponse.Failed("VAULT_CHARGE_FAILED", e.getMessage());
        }finally {
            //empty the pan byte array
            if(decryptedPan != null ) Arrays.fill(decryptedPan, (byte) 0);
        }
    }

    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4")) {
            return CardBrand.VISA;
        } else if (pan.startsWith("5") || pan.startsWith("2")) {
            return CardBrand.MASTERCARD;
        } else if (pan.startsWith("37") || pan.startsWith("34")) {
            return CardBrand.AMEX;
        } else {
            return CardBrand.RUPAY;
        }
    }

    private VaultCard createAndSaveNewVaultCard(TokenizeRequest request, String fingerprint) {
        String pan = request.pan();
        String lastFour = pan.substring(pan.length() - 4);
        String bin = pan.substring(0, 6);
        CardBrand cardBrand = detectBrand(pan);

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(pan.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .fingerprint(fingerprint) // New indexed column
                .lastFour(lastFour)
                .bin(bin)
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .brand(cardBrand)
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .cardHolderName(request.cardHolderName())
                .build();

        return vaultCardRepository.save(vaultCard);
    }

    private CardToken createAndSaveNewToken(VaultCard vaultCard, UUID merchantId, UUID customerId) {
        String tokenStr = "tok_" + RandomizerUtil.randomBase64(32);

        CardToken cardToken = CardToken.builder()
                .token(tokenStr)
                .vaultCard(vaultCard)
                .merchantId(merchantId)
                .customerId(customerId)
                .build();

        return cardTokenRepository.save(cardToken);
    }
}
