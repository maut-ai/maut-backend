package com.maut.core.modules.authenticator.repository;

import com.maut.core.modules.authenticator.model.WebauthnRegistrationChallenge;
import com.maut.core.modules.user.model.MautUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebauthnRegistrationChallengeRepository extends JpaRepository<WebauthnRegistrationChallenge, UUID> {

    Optional<WebauthnRegistrationChallenge> findByChallenge(String challenge);

    Optional<WebauthnRegistrationChallenge> findByMautUserAndChallengeAndExpiresAtAfter(
            MautUser mautUser, String challenge, OffsetDateTime currentTime);

    void deleteAllByExpiresAtBefore(OffsetDateTime expiryTime);
    
    void deleteByChallenge(String challenge);
}
