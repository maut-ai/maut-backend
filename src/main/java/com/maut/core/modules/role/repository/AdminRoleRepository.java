package com.maut.core.modules.role.repository;

import com.maut.core.modules.role.model.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, UUID> {
    Optional<AdminRole> findByName(String name);
}
