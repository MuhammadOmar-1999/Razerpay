package com.omar.razerpay.vault.service.impl;

import com.omar.razerpay.common.entity.Money;
import com.omar.razerpay.common.enums.CardBrand;
import com.omar.razerpay.common.util.RandomizerUtil;
import com.omar.razerpay.payment.Processor.PaymentProcessor;
import com.omar.razerpay.payment.Processor.PaymentProcessorRouter;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorRequest;
import com.omar.razerpay.payment.Processor.dto.PaymentProcessorResponse;
import com.omar.razerpay.vault.config.VaultEncryptionConfig;
import com.omar.razerpay.vault.dto.request.TokenizeRequest;
import com.omar.razerpay.vault.dto.response.TokenizeResponse;
import com.omar.razerpay.vault.entity.CardToken;
import com.omar.razerpay.vault.entity.VaultCard;
import com.omar.razerpay.vault.repository.CardTokenRepository;
import com.omar.razerpay.vault.repository.VaultCardRepository;
import com.omar.razerpay.vault.service.VaultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String lastFour = request.pan().substring(request.pan().length() - 4);
        String bin = request.pan().substring(0, 6);
        CardBrand cardBrand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();

        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .lastFour(lastFour)
                .bin(bin)
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .brand(cardBrand.name())
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .cardHolderName(request.cardHolderName())
                .build();

        vaultCardRepository.save(vaultCard);

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        CardToken cardToken = CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .merchantId(merchantId)
                .customerId(request.customerId())
                .build();

        cardTokenRepository.save(cardToken);

        return TokenizeResponse.builder()
                .token(token)
                .lastFour(lastFour)
                .bin(bin)
                .brand(cardBrand)
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
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
}
