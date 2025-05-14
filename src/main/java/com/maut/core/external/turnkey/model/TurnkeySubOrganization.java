package com.maut.core.external.turnkey.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnkeySubOrganization {
    private String subOrganizationId; // Renamed from id
    private String name; // e.g., "wallet-for-user-uuid"
    // Add any other relevant fields returned by Turnkey for a sub-organization
}
