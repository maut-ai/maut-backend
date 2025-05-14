package com.maut.core.external.turnkey.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TurnkeyAuthenticator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String getStamp(String data, String apiKey, String apiSecret) {
        if (data == null || apiKey == null || apiSecret == null) {
            log.error("Cannot generate Turnkey stamp: data, apiKey, or apiSecret is null.");
            throw new IllegalArgumentException("Stamp generation components cannot be null.");
        }
        try {
            Mac sha256Hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256Hmac.init(secretKeySpec);
            byte[] hashedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String signature = Hex.encodeHexString(hashedBytes);
            return apiKey + "." + signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating Turnkey stamp: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Turnkey API stamp due to cryptographic error", e);
        }
    }
}
