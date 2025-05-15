package com.maut.core.modules.authenticator.repository;

import com.maut.core.modules.authenticator.model.MautUserWebauthnCredential;
import com.maut.core.modules.user.model.MautUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MautUserWebauthnCredentialRepository extends JpaRepository<MautUserWebauthnCredential, UUID> {

    Optional<MautUserWebauthnCredential> findByExternalId(String externalId);

    List<MautUserWebauthnCredential> findAllByMautUser(MautUser mautUser);

    Optional<MautUserWebauthnCredential> findByMautUserAndExternalId(MautUser mautUser, String externalId);

    boolean existsByMautUserAndFriendlyName(MautUser mautUser, String friendlyName);

}
