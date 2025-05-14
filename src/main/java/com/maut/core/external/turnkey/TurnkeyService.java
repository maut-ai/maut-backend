package com.maut.core.external.turnkey;

import com.maut.core.external.turnkey.model.TurnkeyPrivateKey;
import com.maut.core.external.turnkey.model.TurnkeySubOrganization;
import com.maut.core.integration.turnkey.exception.TurnkeyOperationException;

public interface TurnkeyService {

    /**
     * Creates a new sub-organization in Turnkey.
     * @param subOrganizationName The desired name for the sub-organization.
     * @return The created TurnkeySubOrganization.
     * @throws TurnkeyOperationException if the operation fails.
     */
    TurnkeySubOrganization createSubOrganization(String subOrganizationName) throws TurnkeyOperationException;

    /**
     * Creates a new Maut-managed private key within a specified Turnkey sub-organization.
     * This key is primarily managed by the Maut system.
     * @param subOrganizationId The ID of the sub-organization where the key will be created.
     * @param privateKeyName The desired name for the private key.
     * @return The created TurnkeyPrivateKey.
     * @throws TurnkeyOperationException if the operation fails.
     */
    TurnkeyPrivateKey createMautManagedPrivateKey(String subOrganizationId, String privateKeyName) throws TurnkeyOperationException;

    /**
     * Creates a new user-controlled private key within a specified Turnkey sub-organization.
     * This key is intended to be more directly associated with the end-user.
     * @param subOrganizationId The ID of the sub-organization where the key will be created.
     * @param userId The MautUser's ID, for associating/naming the key.
     * @param privateKeyName The desired name for the private key.
     * @return The created TurnkeyPrivateKey.
     * @throws TurnkeyOperationException if the operation fails.
     */
    TurnkeyPrivateKey createUserControlledPrivateKey(String subOrganizationId, String userId, String privateKeyName) throws TurnkeyOperationException;

    // Optional: Method to assign a default policy
    /**
     * Assigns a default policy to a private key in Turnkey.
     * @param privateKeyId The ID of the private key.
     * @param policyId The ID of the policy to assign.
     * @return The ID of the assigned policy activity or confirmation.
     * @throws TurnkeyOperationException if the operation fails.
     */
    // String assignDefaultPolicyToPrivateKey(String privateKeyId, String policyId) throws TurnkeyOperationException;
}
