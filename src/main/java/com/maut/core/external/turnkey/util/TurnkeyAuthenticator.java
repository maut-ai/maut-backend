package com.maut.core.external.turnkey.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TurnkeyAuthenticator {

    private static final String ECDSA_ALGORITHM = "SHA256withECDSA";
    private static final String KEY_ALGORITHM = "EC";
    private static final String SIGNATURE_SCHEME = "SIGNATURE_SCHEME_TK_API_P256";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Add BouncyCastle as a security provider if it's not already present
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String getStamp(String requestBody, String hexPublicKey, String hexPrivateKey) {
        if (requestBody == null || hexPublicKey == null || hexPrivateKey == null) {
            log.error("Cannot generate Turnkey stamp: requestBody, hexPublicKey, or hexPrivateKey is null.");
            throw new IllegalArgumentException("Stamp generation components cannot be null.");
        }

        try {
            // 1. Decode the hex-encoded private key string into a PrivateKey object
            byte[] privateKeyBytes = Hex.decodeHex(hexPrivateKey);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            // 2. Sign the requestBody (JSON string) using SHA256withECDSA
            Signature ecdsaSign = Signature.getInstance(ECDSA_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(requestBody.getBytes(StandardCharsets.UTF_8));
            byte[] derSignature = ecdsaSign.sign();

            // 3. Hex encode the DER signature
            String hexSignature = Hex.encodeHexString(derSignature);

            // 4. Create the JSON stamp object
            ObjectNode stampNode = objectMapper.createObjectNode();
            stampNode.put("publicKey", hexPublicKey);
            stampNode.put("signature", hexSignature);
            stampNode.put("scheme", SIGNATURE_SCHEME);
            String jsonStamp = objectMapper.writeValueAsString(stampNode);

            // 5. Base64URL encode the JSON stamp
            return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonStamp.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                 SignatureException | DecoderException | com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error generating Turnkey API stamp: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Turnkey API stamp due to cryptographic, encoding, or JSON processing error", e);
        } catch (NoSuchProviderException e) {
            log.error("Error generating Turnkey API stamp - BouncyCastle provider not found: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Turnkey API stamp due to missing BouncyCastle provider", e);
        }
    }
}
