package com.maut.core.modules.clientapplication.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateClientApplicationRequest {

    @NotBlank(message = "Client name is mandatory")
    @Size(min = 3, max = 100, message = "Client name must be between 3 and 100 characters")
    private String clientName;
}
