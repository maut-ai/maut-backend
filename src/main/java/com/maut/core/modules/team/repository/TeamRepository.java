package com.maut.core.modules.team.repository;

import com.maut.core.modules.team.model.Team;
import com.maut.core.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);
    boolean existsByName(String name);
    Optional<Team> findByOwner(User owner);
}
