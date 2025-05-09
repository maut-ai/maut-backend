package com.maut.core.modules.clientapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientApplicationResponse {

    private String mautApiClientId;
    private String clientName;
    private String clientSecret; // Plaintext secret, to be returned only upon creation
}
