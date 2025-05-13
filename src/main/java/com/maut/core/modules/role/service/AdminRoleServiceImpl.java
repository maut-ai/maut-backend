package com.maut.core.modules.role.service;

import com.maut.core.modules.role.dto.AdminRoleResponse;
import com.maut.core.modules.role.dto.CreateAdminRoleRequest;
import com.maut.core.modules.role.model.AdminRole;
import com.maut.core.modules.role.repository.AdminRoleRepository;
import com.maut.core.common.exception.DuplicateResourceException; // Will need to create this
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRoleServiceImpl implements AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    @Override
    @Transactional
    public AdminRoleResponse createAdminRole(CreateAdminRoleRequest request) {
        log.info("Attempting to create new admin role with name: {}", request.getName());

        // Check if role with the same name already exists
        if (adminRoleRepository.findByName(request.getName()).isPresent()) {
            log.warn("Admin role with name '{}' already exists.", request.getName());
            throw new DuplicateResourceException("Admin role with name '" + request.getName() + "' already exists.");
        }

        AdminRole adminRole = new AdminRole();
        adminRole.setName(request.getName());
        // createdAt and updatedAt are handled by @CreationTimestamp and @UpdateTimestamp

        AdminRole savedAdminRole = adminRoleRepository.save(adminRole);
        log.info("Successfully created admin role '{}' with ID: {}", savedAdminRole.getName(), savedAdminRole.getId());

        return AdminRoleResponse.fromEntity(savedAdminRole);
    }
}
