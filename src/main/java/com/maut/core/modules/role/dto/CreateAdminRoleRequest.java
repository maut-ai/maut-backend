package com.maut.core.modules.role.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CreateAdminRoleRequest {

    @NotBlank(message = "Role name is mandatory")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;
}
