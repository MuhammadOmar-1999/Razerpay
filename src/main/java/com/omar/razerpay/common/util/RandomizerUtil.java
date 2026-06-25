package com.omar.razerpay.common.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class RandomizerUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static String randomBase64(int length){
        byte[] buf = new byte[(int) Math.ceil(length * 3.0 / 4.0)];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
