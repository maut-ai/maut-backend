package com.maut.core.modules.role.controller;

import com.maut.core.modules.role.dto.AdminRoleResponse;
import com.maut.core.modules.role.dto.CreateAdminRoleRequest;
import com.maut.core.modules.role.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/adminrole")
@RequiredArgsConstructor
@Slf4j
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN_SUPER_ADMIN')")
    public ResponseEntity<AdminRoleResponse> createAdminRole(@Valid @RequestBody CreateAdminRoleRequest request) {
        log.info("Received request to create admin role with name: {}", request.getName());
        // The DuplicateResourceException will be handled by Spring's default mechanisms 
        // or a global exception handler if defined, returning 409.
        AdminRoleResponse response = adminRoleService.createAdminRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Future endpoints for GET, PUT, DELETE admin roles can be added here.
}
