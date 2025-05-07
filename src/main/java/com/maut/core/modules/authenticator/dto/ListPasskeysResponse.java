package com.maut.core.modules.authenticator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPasskeysResponse {

    private List<PasskeyListItem> passkeys;
    private int limit;
    private int offset;
    private long totalPasskeys; // Total number of passkeys for the user (for pagination)

}
