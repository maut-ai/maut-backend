package com.maut.core.external.turnkey;

import com.maut.core.external.turnkey.model.TurnkeyPrivateKey;
import com.maut.core.external.turnkey.model.TurnkeySubOrganization;
import com.maut.core.external.turnkey.model.request.CreatePrivateKeysParameters;
import com.maut.core.external.turnkey.model.request.CreatePrivateKeysRequest;
import com.maut.core.external.turnkey.model.request.CreateSubOrganizationRequest;
import com.maut.core.external.turnkey.model.request.PrivateKeySpecification;
import com.maut.core.external.turnkey.model.request.RootUserPayload;
import com.maut.core.external.turnkey.model.request.SubOrganizationParameters;
import com.maut.core.external.turnkey.model.response.ActivityResponsePayload;
import com.maut.core.external.turnkey.model.response.Address;
import com.maut.core.external.turnkey.model.response.CreatePrivateKeysActivityResult;
import com.maut.core.external.turnkey.model.response.CreateSubOrgActivityResult;
import com.maut.core.external.turnkey.model.response.PrivateKeyDetails;
import com.maut.core.external.turnkey.model.response.SubOrganizationDetails;
import com.maut.core.external.turnkey.model.response.TurnkeyActivityResponseWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import com.maut.core.external.turnkey.util.TurnkeyAuthenticator;

@Service
@Slf4j
public class TurnkeyServiceImpl implements TurnkeyService {

    private static final String ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7 = "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7";
    private static final String ACTIVITY_TYPE_CREATE_PRIVATE_KEYS_V2 = "ACTIVITY_TYPE_CREATE_PRIVATE_KEYS_V2";

    // TODO: Make these configurable if necessary
    private static final String DEFAULT_PRIVATE_KEY_ALGORITHM = "ALGORITHM_ECDSA";
    private static final String DEFAULT_PRIVATE_KEY_CURVE = "CURVE_SECP256K1";
    private static final String DEFAULT_ADDRESS_FORMAT = "ADDRESS_FORMAT_ETHEREUM";

    private final RestTemplate restTemplate;

    @Value("${turnkey.api.baseUrl}")
    private String turnkeyApiBaseUrl;

    @Value("${turnkey.api.key}")
    private String turnkeyApiKey;

    @Value("${turnkey.api.secret}")
    private String turnkeyApiSecret;

    @Value("${turnkey.api.organizationId}")
    private String turnkeyOrganizationId;

    public TurnkeyServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public TurnkeySubOrganization createSubOrganization(String subOrganizationName) {
        String url = turnkeyApiBaseUrl + "/public/v1/submit/create_sub_organization";
        log.info("Attempting to create Turnkey sub-organization '{}' for organization ID: {}. URL: {}", subOrganizationName, turnkeyOrganizationId, url);

        CreateSubOrganizationRequest requestPayload = CreateSubOrganizationRequest.builder()
            .type(ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7)
            .timestampMs(String.valueOf(System.currentTimeMillis()))
            .organizationId(turnkeyOrganizationId)
            .parameters(SubOrganizationParameters.builder()
                    .subOrganizationName(subOrganizationName)
                    .rootUsers(Collections.singletonList(RootUserPayload.builder()
                            .userName("root")
                            .apiKeys(Collections.emptyList())
                            .authenticators(Collections.emptyList())
                            .oauthProviders(Collections.emptyList())
                            .build()))
                    .rootQuorumThreshold(1)
                    .build())
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Stamp", TurnkeyAuthenticator.getStamp(convertPayloadToString(requestPayload), turnkeyApiKey, turnkeyApiSecret));

        HttpEntity<CreateSubOrganizationRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            log.debug("Turnkey Request Payload: {}", requestPayload);

            ResponseEntity<TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreateSubOrgActivityResult>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreateSubOrgActivityResult>>>() {}
            );
            log.info("Turnkey API response status: {}", response.getStatusCode());
            log.debug("Turnkey API response body: {}", response.getBody());

            TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreateSubOrgActivityResult>> responseBody = response.getBody();
            if (responseBody != null && responseBody.getActivity() != null && responseBody.getActivity().getResult() != null &&
                responseBody.getActivity().getResult().getCreateSubOrganizationResultV7() != null) {
                SubOrganizationDetails subOrgDetails = responseBody.getActivity().getResult().getCreateSubOrganizationResultV7();
                if (subOrgDetails.getSubOrganizationId() != null) {
                    String newSubOrganizationId = subOrgDetails.getSubOrganizationId();
                    log.info("Successfully created Turnkey sub-organization with ID: {}", newSubOrganizationId);
                    return TurnkeySubOrganization.builder()
                            .subOrganizationId(newSubOrganizationId)
                            .name(subOrganizationName)
                            .build();
                }
            }
            // If any part of the expected structure is missing, log an error
            log.error("Failed to create Turnkey sub-organization: Invalid or incomplete response structure. Response: {}", responseBody);
            // Consider throwing a specific exception for clarity and better error handling downstream
            throw new RuntimeException("Failed to create Turnkey sub-organization due to invalid or incomplete response.");

        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException while creating Turnkey sub-organization: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error creating Turnkey sub-organization: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) { // More general RestClientException
            log.error("RestClientException while creating Turnkey sub-organization: {}", e.getMessage(), e);
            throw new RuntimeException("Network or communication error creating Turnkey sub-organization", e);
        } catch (Exception e) { // Catch-all for any other unexpected exceptions
            log.error("Unexpected exception while creating Turnkey sub-organization: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error creating Turnkey sub-organization", e);
        }
    }

    @Override
    public TurnkeyPrivateKey createMautManagedPrivateKey(String subOrganizationId, String privateKeyName) {
        String timestampMs = String.valueOf(System.currentTimeMillis());
        PrivateKeySpecification keySpec = PrivateKeySpecification.builder()
            .privateKeyName(privateKeyName)
            .algorithm(DEFAULT_PRIVATE_KEY_ALGORITHM)
            .curve(DEFAULT_PRIVATE_KEY_CURVE)
            .tags(Collections.emptyList()) // Example: could add tags like "maut-managed", "env:production"
            .build();

        CreatePrivateKeysParameters params = CreatePrivateKeysParameters.builder()
            .subOrganizationId(subOrganizationId)
            .privateKeys(Collections.singletonList(keySpec))
            .build();

        CreatePrivateKeysRequest requestPayload = CreatePrivateKeysRequest.builder()
            .type(ACTIVITY_TYPE_CREATE_PRIVATE_KEYS_V2)
            .timestampMs(timestampMs)
            .organizationId(turnkeyOrganizationId)
            .parameters(params)
            .build();

        String requestPayloadString = convertPayloadToString(requestPayload);
        // A null check for requestPayloadString is good practice, though convertPayloadToString throws an exception on failure.
        // However, defensive programming might still warrant it if convertPayloadToString could be refactored to return null.
        if (requestPayloadString == null) {
             log.error("Request payload string is null for createMautManagedPrivateKey. SubOrgID: {}, KeyName: {}", subOrganizationId, privateKeyName);
            throw new RuntimeException("Failed to serialize request payload for Turnkey API stamp generation.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Stamp", TurnkeyAuthenticator.getStamp(requestPayloadString, turnkeyApiKey, turnkeyApiSecret));

        HttpEntity<CreatePrivateKeysRequest> entity = new HttpEntity<>(requestPayload, headers);

        String url = turnkeyApiBaseUrl + "/public/v1/submit/create_private_keys";
        log.info("Sending Turnkey request to create Maut-managed private key. URL: {}, SubOrgID: {}, KeyName: {}", url, subOrganizationId, privateKeyName);

        try {
            ResponseEntity<TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreatePrivateKeysActivityResult>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreatePrivateKeysActivityResult>>>() {}
            );

            log.info("Received Turnkey response for Maut-managed private key creation. Status: {}", response.getStatusCode());

            TurnkeyActivityResponseWrapper<ActivityResponsePayload<CreatePrivateKeysActivityResult>> responseBody = response.getBody();
            if (responseBody != null && responseBody.getActivity() != null && responseBody.getActivity().getResult() != null &&
                responseBody.getActivity().getResult().getCreatePrivateKeysResultV2() != null &&
                responseBody.getActivity().getResult().getCreatePrivateKeysResultV2().getPrivateKeys() != null &&
                !responseBody.getActivity().getResult().getCreatePrivateKeysResultV2().getPrivateKeys().isEmpty()) {

                PrivateKeyDetails pkDetails = responseBody.getActivity().getResult().getCreatePrivateKeysResultV2().getPrivateKeys().get(0);
                String keyAddress = null;
                if (pkDetails.getAddresses() != null) {
                    keyAddress = pkDetails.getAddresses().stream()
                        .filter(addr -> DEFAULT_ADDRESS_FORMAT.equals(addr.getFormat()))
                        .map(Address::getAddress)
                        .findFirst()
                         // Fallback to the first address if the default format is not found, or if addresses list is empty.
                        .orElse(pkDetails.getAddresses().isEmpty() ? null : pkDetails.getAddresses().get(0).getAddress());
                }

                if (pkDetails.getPrivateKeyId() == null || keyAddress == null) {
                    log.error("Failed to extract privateKeyId or address from Turnkey response for keyName: {}. Details: {}", privateKeyName, pkDetails);
                    throw new RuntimeException("Turnkey private key creation succeeded but response format was unexpected (missing ID or address).");
                }

                log.info("Successfully created Maut-managed private key. Name: {}, ID: {}, Address: {}", privateKeyName, pkDetails.getPrivateKeyId(), keyAddress);
                return TurnkeyPrivateKey.builder()
                        .privateKeyId(pkDetails.getPrivateKeyId())
                        .address(keyAddress)
                        .build();
            } else {
                log.error("Turnkey Maut-managed private key creation for name '{}' failed or response format was unexpected. Response: {}", privateKeyName, responseBody);
                throw new RuntimeException("Turnkey Maut-managed private key creation failed or response format was unexpected.");
            }
        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException while creating Maut-managed private key (name: {}): {} - {}", privateKeyName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error creating Maut-managed private key: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("RestClientException while creating Maut-managed private key (name: {}): {}", privateKeyName, e.getMessage(), e);
            throw new RuntimeException("Network or communication error creating Maut-managed private key", e);
        } catch (Exception e) {
            log.error("Unexpected exception while creating Maut-managed private key (name: {}): {}", privateKeyName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error creating Maut-managed private key", e);
        }
    }

    @Override
    public TurnkeyPrivateKey createUserControlledPrivateKey(String subOrganizationId, String userId, String privateKeyName) {
        // TODO: Implement logic similar to createMautManagedPrivateKey,
        // but tailored for user-controlled keys.
        // This will likely involve different parameters or a different Turnkey endpoint/activity type.
        // For now, returning a placeholder or throwing an UnsupportedOperationException.
        log.warn("createUserControlledPrivateKey is not yet implemented. SubOrgID: {}, UserID: {}, PrivateKeyName: {}", subOrganizationId, userId, privateKeyName);
        throw new UnsupportedOperationException("createUserControlledPrivateKey is not yet implemented.");
    }

    private String convertPayloadToString(Object payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ensure this is consistent with Turnkey's expectations if re-enabled. Usually handled by DTO annotations (@JsonInclude).
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Error converting payload to string for X-Stamp generation", e);
            // Propagate as a runtime exception because this is a critical step.
            throw new RuntimeException("Failed to serialize payload to string", e);
        }
    }
}
