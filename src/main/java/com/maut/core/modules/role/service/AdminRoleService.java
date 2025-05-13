package com.maut.core.modules.role.service;

import com.maut.core.modules.role.dto.AdminRoleResponse;
import com.maut.core.modules.role.dto.CreateAdminRoleRequest;

public interface AdminRoleService {
    AdminRoleResponse createAdminRole(CreateAdminRoleRequest request);
    // Future methods like findById, findAll, deleteById can be added here
}
