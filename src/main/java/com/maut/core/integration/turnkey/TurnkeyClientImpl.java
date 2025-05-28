package com.maut.core.integration.turnkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.integration.turnkey.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// Added imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maut.core.external.turnkey.util.TurnkeyAuthenticator;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TurnkeyClientImpl implements TurnkeyClient {

    private static final Logger logger = LoggerFactory.getLogger(TurnkeyClientImpl.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${turnkey.api.baseUrl}")
    private String turnkeyApiBaseUrl;

    @Value("${turnkey.api.key}") // This is the public key part of the API key pair
    private String turnkeyApiKeyHex;

    @Value("${turnkey.api.secret}") // This is the private key part of the API key pair
    private String turnkeyApiSecretHex;

    @Value("${turnkey.api.organizationId}") // Parent Organization ID making the API call
    private String turnkeyParentOrganizationId;

    // Constructor for dependency injection
    public TurnkeyClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Data
    @Builder
    private static class TurnkeyActivityRequest<P> {
        @JsonProperty("type")
        private String type;
        @JsonProperty("timestampMs")
        private String timestampMs;
        @JsonProperty("organizationId")
        private String organizationId;
        @JsonProperty("parameters")
        private P parameters;
        @JsonProperty("fingerprint")
        private String fingerprint;
    }

    private <P_PARAMS, R_WRAPPER> R_WRAPPER postTurnkeyActivity(
            String activityType,
            String targetOrganizationId, // ID of the org to perform activity in (parent or sub-org)
            P_PARAMS parameters, // The 'parameters' object for the Turnkey activity
            Class<R_WRAPPER> responseWrapperClass) {

        TurnkeyActivityRequest<P_PARAMS> activityRequest = TurnkeyActivityRequest.<P_PARAMS>builder()
                .type(activityType)
                .organizationId(targetOrganizationId) // Use the passed targetOrganizationId
                .timestampMs(String.valueOf(System.currentTimeMillis()))
                .parameters(parameters)
                .fingerprint(UUID.randomUUID().toString())
                .build();

        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(activityRequest);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing Turnkey request for activity {}: {}", activityType, e.getMessage());
            throw new RuntimeException("Failed to serialize request to Turnkey for activity: " + activityType, e);
        }

        String stamp = TurnkeyAuthenticator.getStamp(requestBodyJson, turnkeyApiKeyHex, turnkeyApiSecretHex);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Stamp", stamp);

        HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);
        String url = turnkeyApiBaseUrl + "/public/v1/submit_activity";

        try {
            logger.debug("Sending Turnkey {} request to {}: Body: {}", activityType, url, requestBodyJson);
            R_WRAPPER response = restTemplate.postForObject(url, entity, responseWrapperClass);
            logger.debug("Received Turnkey {} response: {}", activityType, response);
            return response;
        } catch (RestClientException e) {
            logger.error("Error calling Turnkey API for activity {}: {}", activityType, e.getMessage(), e);
            throw new RuntimeException("Failed to call Turnkey API for activity: " + activityType, e);
        }
    }

    @Override
    public TurnkeyCreateSubOrganizationResponse createSubOrganizationWithPasskey(TurnkeyCreateSubOrganizationRequest request) {

        // The request object itself is now the 'requestParams' to be sent to Turnkey.
        // We assume the caller has fully constructed the TurnkeyCreateSubOrganizationRequest object
        // with all necessary nested details (RootUserParams, WalletParams, etc.)

        try {
            TurnkeyCreateSubOrganizationResponse.TurnkeyActivityResponseWrapper responseWrapper = postTurnkeyActivity(
                    "CREATE_SUB_ORGANIZATION_V7",
                    turnkeyParentOrganizationId, // Activity occurs under the parent org
                    request, // The TurnkeyCreateSubOrganizationRequest is the 'parameters'
                    TurnkeyCreateSubOrganizationResponse.TurnkeyActivityResponseWrapper.class
            );

            if (responseWrapper != null && responseWrapper.getActivity() != null &&
                responseWrapper.getActivity().getResult() != null &&
                responseWrapper.getActivity().getResult().getActivity() != null &&
                responseWrapper.getActivity().getResult().getActivity().getResult() != null &&
                responseWrapper.getActivity().getResult().getActivity().getResult().getCreateSubOrganizationResultV7() != null) {

                TurnkeyCreateSubOrganizationResponse.CreateSubOrganizationResultV7 resultV7 =
                        responseWrapper.getActivity().getResult().getActivity().getResult().getCreateSubOrganizationResultV7();

                String createdSubOrgId = resultV7.getSubOrganizationId();
                String userKeyId = null;
                String userKeyAddress = null;

                if (resultV7.getWallet() != null) {
                    userKeyId = resultV7.getWallet().getWalletId(); // Assuming walletId is the user's primary key ID
                    if (resultV7.getWallet().getAddresses() != null && !resultV7.getWallet().getAddresses().isEmpty()) {
                        userKeyAddress = resultV7.getWallet().getAddresses().get(0);
                    }
                }

                boolean success = "ACTIVITY_STATUS_COMPLETED".equals(responseWrapper.getActivity().getStatus());

                return TurnkeyCreateSubOrganizationResponse.builder()
                        .success(success)
                        .subOrganizationId(createdSubOrgId)
                        .userPrivateKeyId(userKeyId)
                        .userPrivateKeyAddress(userKeyAddress)
                        .build();
            } else {
                logger.error("Failed to parse create_sub_organization_v7 response from Turnkey. Wrapper or nested fields are null.");
                return TurnkeyCreateSubOrganizationResponse.builder().success(false).errorMessage("Failed to parse response from Turnkey.").build();
            }
        } catch (Exception e) {
            // Attempt to get a user identifier from the request for logging, if available
            String userIdForLog = "<unknown_user>";
            if (request != null && request.getRootUsers() != null && !request.getRootUsers().isEmpty() && request.getRootUsers().get(0) != null) {
                userIdForLog = request.getRootUsers().get(0).getUserName();
            }
            logger.error("Error during createSubOrganizationWithPasskey for user {}: {}", userIdForLog, e.getMessage(), e);
            return TurnkeyCreateSubOrganizationResponse.builder().success(false).errorMessage(e.getMessage()).build();
        }
    }

    @Override
    public TurnkeyCreateMautManagedKeyResponse createMautManagedKey(TurnkeyCreateMautManagedKeyRequest request) {
        if (request == null || request.getSubOrganizationId() == null || request.getPrivateKeys() == null || request.getPrivateKeys().isEmpty()) {
            logger.error("Invalid request for createMautManagedKey: request, subOrganizationId, or privateKeys list is null/empty.");
            return TurnkeyCreateMautManagedKeyResponse.builder()
                    .success(false)
                    .errorMessage("Invalid request parameters.")
                    .build();
        }

        // Parameters for the CREATE_PRIVATE_KEYS_V2 activity.
        // Turnkey expects the parameters for this activity type to be an object like: {"privateKeys": [...]}
        Map<String, List<TurnkeyCreateMautManagedKeyRequest.PrivateKeyParams>> activityParameters = 
                Map.of("privateKeys", request.getPrivateKeys());

        try {
            TurnkeyCreateMautManagedKeyResponse.TurnkeyActivityResponseWrapper responseWrapper = postTurnkeyActivity(
                    "CREATE_PRIVATE_KEYS_V2",
                    request.getSubOrganizationId(), // Activity occurs within this sub-organization
                    activityParameters, // Map.of("privateKeys", request.getPrivateKeys())
                    TurnkeyCreateMautManagedKeyResponse.TurnkeyActivityResponseWrapper.class
            );

            if (responseWrapper != null && responseWrapper.getActivity() != null) {
                TurnkeyCreateMautManagedKeyResponse.Activity activity = responseWrapper.getActivity();
                if ("ACTIVITY_STATUS_COMPLETED".equals(activity.getStatus()) && 
                    activity.getResult() != null && 
                    activity.getResult().getCreatePrivateKeysResultV2() != null && 
                    activity.getResult().getCreatePrivateKeysResultV2().getPrivateKeys() != null && 
                    !activity.getResult().getCreatePrivateKeysResultV2().getPrivateKeys().isEmpty()) {
                    
                    // Assuming we are interested in the first key created if multiple were requested.
                    TurnkeyCreateMautManagedKeyResponse.PrivateKeyDetails pkDetails = activity.getResult().getCreatePrivateKeysResultV2().getPrivateKeys().get(0);
                    String address = null;
                    if (pkDetails.getAddresses() != null && !pkDetails.getAddresses().isEmpty()) {
                        address = pkDetails.getAddresses().get(0).getAddress(); // Take the first address
                    }

                    return TurnkeyCreateMautManagedKeyResponse.builder()
                            .success(true)
                            .privateKeyId(pkDetails.getPrivateKeyId())
                            .privateKeyName(pkDetails.getPrivateKeyName())
                            .privateKeyAddress(address)
                            .build();
                } else {
                    String errorMessage = "Turnkey activity CREATE_PRIVATE_KEYS_V2 did not complete successfully or returned no key details.";
                    if (activity.getResult() != null && activity.getResult().getCreatePrivateKeysResultV2() != null && 
                        (activity.getResult().getCreatePrivateKeysResultV2().getPrivateKeys() == null || 
                         activity.getResult().getCreatePrivateKeysResultV2().getPrivateKeys().isEmpty())) {
                        errorMessage = "Turnkey activity CREATE_PRIVATE_KEYS_V2 completed but returned no private keys.";
                    } else if (!"ACTIVITY_STATUS_COMPLETED".equals(activity.getStatus())){
                        errorMessage = "Turnkey activity status: " + activity.getStatus();
                    }
                    logger.error("{}: Activity ID: {}", errorMessage, activity.getId());
                    return TurnkeyCreateMautManagedKeyResponse.builder().success(false).errorMessage(errorMessage).build();
                }
            } else {
                logger.error("Failed to create Maut-managed key: No response or activity data from Turnkey.");
                return TurnkeyCreateMautManagedKeyResponse.builder().success(false).errorMessage("No response from Turnkey.").build();
            }
        } catch (Exception e) {
            logger.error("Error during createMautManagedKey for subOrg {}: {}", request.getSubOrganizationId(), e.getMessage(), e);
            return TurnkeyCreateMautManagedKeyResponse.builder().success(false).errorMessage(e.getMessage()).build();
        }
    }

    // --- Existing placeholder passkey methods --- 
    @Override
    public TurnkeyInitiatePasskeyRegistrationResponse initiatePasskeyRegistration(TurnkeyInitiatePasskeyRegistrationRequest request) {
        logger.info("Placeholder: Initiating passkey registration with Turnkey for user: {}", request.getMautUserId());
        // This is a placeholder implementation.
        // In a real scenario, this method would call the Turnkey API to initiate registration.
        String publicKeyCredentialCreationOptions = "{\"rp\":{\"name\":\"Maut Demo\",\"id\":\"localhost\"},\"user\":{\"id\":\"" + request.getMautUserId() + "\",\"name\":\"testuser@example.com\",\"displayName\":\"Test User\"},\"challenge\":\"mock-challenge-data\",\"pubKeyCredParams\":[{\"type\":\"public-key\",\"alg\":-7},{\"type\":\"public-key\",\"alg\":-257}]}";
        return TurnkeyInitiatePasskeyRegistrationResponse.builder()
                .challenge("mock-turnkey-challenge-" + System.currentTimeMillis()) // Example challenge
                .turnkeyAttestationId("mock-attestation-id-" + System.currentTimeMillis()) // Example attestation ID
                .publicKeyCredentialCreationOptions(publicKeyCredentialCreationOptions) // Simplified example
                .build();
    }

    @Override
    public TurnkeyFinalizePasskeyRegistrationResponse finalizePasskeyRegistration(TurnkeyFinalizePasskeyRegistrationRequest request) {
        logger.info("Placeholder: Finalizing passkey registration with Turnkey using context: {}", request.getRegistrationContextId());
        // This is a placeholder implementation.
        // In a real scenario, this method would call the Turnkey API to finalize registration.
        // It would verify the attestation object and create the passkey.
        return TurnkeyFinalizePasskeyRegistrationResponse.builder()
                .success(true)
                .turnkeyAuthenticatorId("mock-turnkey-authenticator-id-" + System.currentTimeMillis()) // Example authenticator ID
                .publicKey("mock-public-key-data")
                .build();
    }

    @Override
    public TurnkeyVerifyAssertionResponse verifyPasskeyAssertion(TurnkeyVerifyAssertionRequest request) {
        logger.info("Placeholder: Verifying passkey assertion with Turnkey for credentialId: {}", request.getPasskeyCredentialId());
        // This is a placeholder implementation.
        // In a real scenario, this method would call the Turnkey API to verify the assertion.
        return TurnkeyVerifyAssertionResponse.builder()
                .success(true) // Assume success for placeholder
                .mautUserId("user-id-from-verified-passkey") // This would be determined by Turnkey/our mapping
                .turnkeyAuthenticatorId(request.getPasskeyCredentialId())
                .build();
    }
}
