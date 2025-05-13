package com.maut.core.modules.role.repository;

import com.maut.core.modules.role.model.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRoleRepository extends JpaRepository<TeamRole, UUID> {
    Optional<TeamRole> findByName(String name);
}
