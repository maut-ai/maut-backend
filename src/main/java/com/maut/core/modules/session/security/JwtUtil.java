package com.maut.core.modules.session.security;

import com.maut.core.modules.clientapplication.model.ClientApplication; // Updated import
import com.maut.core.modules.user.model.MautUser; // Updated import
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${maut.jwt.session.secret:DefaultPlaceholderSecretForMautSessionTokensWhichIsVeryLongAndSecure}")
    private String mautSessionTokenSecretString;

    @Value("${maut.jwt.session.expirationMs:3600000}") // 1 hour default
    private long mautSessionExpirationMs;

    @Value("${maut.jwt.client.expectedAudience:https://api.maut.ai}") // Expected audience for client tokens
    private String expectedClientTokenAudience;

    private SecretKey mautSessionSigningKey;

    @javax.annotation.PostConstruct
    private void init() {
        if (mautSessionTokenSecretString == null || mautSessionTokenSecretString.getBytes().length < 32) { 
            System.err.println("WARNING: maut.jwt.session.secret is not configured or is too short (min 32 bytes for HS256). Using a default insecure key for development.");
            this.mautSessionSigningKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.mautSessionSigningKey = Keys.hmacShaKeyFor(mautSessionTokenSecretString.getBytes());
        }
    }

    public String generateMautSessionToken(MautUser mautUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mautUserId", mautUser.getMautUserId().toString());
        return createToken(claims, mautUser.getMautUserId().toString(), mautSessionExpirationMs, mautSessionSigningKey);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTimeMillis, SecretKey signingKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaimsFromMautSession(String token) {
        return Jwts.parserBuilder().setSigningKey(mautSessionSigningKey).build().parseClaimsJws(token).getBody();
    }

    public <T> T extractClaimFromMautSession(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaimsFromMautSession(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpirationFromMautSession(String token) {
        return extractClaimFromMautSession(token, Claims::getExpiration);
    }

    public String extractMautUserIdFromMautSession(String token) {
        return extractClaimFromMautSession(token, claims -> claims.get("mautUserId", String.class));
    }

    private Boolean isTokenExpired(String token, SecretKey signingKey) {
        try {
            return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
        } catch (Exception e) { 
            return true; 
        }
    }

    public Boolean validateMautSessionToken(String token, MautUser mautUser) {
        final String mautUserIdFromToken = extractMautUserIdFromMautSession(token);
        return (mautUser.getMautUserId().toString().equals(mautUserIdFromToken) && !isTokenExpired(token, mautSessionSigningKey));
    }

    public String extractIssuerFromUnverifiedClientToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) { 
                System.err.println("Invalid JWT structure: not enough parts.");
                return null;
            }
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            final String searchKey = "\"iss\":\"";
            int startIndex = payloadJson.indexOf(searchKey);
            if (startIndex == -1) {
                System.err.println("Claim 'iss' not found in client token payload.");
                return null;
            }
            startIndex += searchKey.length();
            int endIndex = payloadJson.indexOf("\"", startIndex);
            if (endIndex == -1) {
                 System.err.println("Malformed 'iss' claim in client token payload.");
                return null;
            }
            return payloadJson.substring(startIndex, endIndex);

        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64URL in client token: " + e.getMessage());
            return null;
        }
         catch (Exception e) {
            System.err.println("Error extracting issuer from unverified client token: " + e.getMessage());
            return null;
        }
    }

    public Boolean validateClientAuthToken(String token, ClientApplication clientApp) { 
        if (clientApp == null || clientApp.getClientSecret() == null) {
            System.err.println("Client application or client secret is null.");
            return false;
        }
        SecretKey clientSigningKey;
        try {
            byte[] secretBytes = clientApp.getClientSecret().getBytes();
            if (secretBytes.length < 32) { 
                 System.err.println("Client secret for app " + clientApp.getMautApiClientId() + " is too short for HS256. THIS IS INSECURE.");
                 return false; 
            }
            clientSigningKey = Keys.hmacShaKeyFor(secretBytes);
        } catch (Exception e) {
            System.err.println("Error creating signing key from client secret for app " + clientApp.getMautApiClientId() + ": " + e.getMessage());
            return false;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(clientSigningKey)
                                .requireAudience(expectedClientTokenAudience) 
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

            if (!clientApp.getMautApiClientId().equals(claims.getIssuer())) {
                System.err.println("Issuer mismatch for client token. Expected: " + clientApp.getMautApiClientId() + ", Got: " + claims.getIssuer());
                return false; 
            }
            
            return !isTokenExpired(token, clientSigningKey);
        } catch (Exception e) {
            System.err.println("Client auth token validation failed for app " + clientApp.getMautApiClientId() + ": " + e.getMessage());
            return false;
        }
    }

    public String extractClientSystemUserIdFromClientToken(String token, ClientApplication clientApp) { 
        SecretKey clientSigningKey;
         try {
            byte[] secretBytes = clientApp.getClientSecret().getBytes();
             if (secretBytes.length < 32) {
                 System.err.println("Client secret for app " + clientApp.getMautApiClientId() + " is too short for HS256 when extracting user ID. THIS IS INSECURE.");
                 return null;
             }
            clientSigningKey = Keys.hmacShaKeyFor(secretBytes);
        } catch (Exception e) {
            System.err.println("Error creating signing key from client secret for app " + clientApp.getMautApiClientId() + " when extracting user ID: " + e.getMessage());
            return null;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(clientSigningKey)
                                .requireAudience(expectedClientTokenAudience) 
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
            
            if (!clientApp.getMautApiClientId().equals(claims.getIssuer())) {
                 System.err.println("Issuer mismatch when extracting user ID from client token. Expected: " + clientApp.getMautApiClientId() + ", Got: " + claims.getIssuer());
                return null; 
            }
            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("Error extracting clientSystemUserId from client token for app " + clientApp.getMautApiClientId() + ": " + e.getMessage());
            return null;
        }
    }
}
