package com.maut.core.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedMautUsersResponseDTO {
    private List<MautUserResponseItemDTO> data;
    private long position; // Represents offset + number of items returned in the current page
    private long recordsTotal;
}
