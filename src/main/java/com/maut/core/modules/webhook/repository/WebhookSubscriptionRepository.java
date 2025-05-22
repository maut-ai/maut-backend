package com.maut.core.modules.webhook.repository;

import com.maut.core.modules.webhook.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    List<WebhookSubscription> findByTeamId(UUID teamId);
    Optional<WebhookSubscription> findByIdAndTeamId(UUID id, UUID teamId);
    boolean existsByTeamIdAndTargetUrlAndActiveTrue(UUID teamId, String targetUrl);
    WebhookSubscription findByTeamIdAndTargetUrlAndActiveTrue(UUID teamId, String targetUrl); // Added for the update conflict check

    @Query("SELECT ws FROM WebhookSubscription ws WHERE ws.teamId = :teamId AND ws.active = true AND " +
           "(ws.eventTypes = :eventType OR " +
           "ws.eventTypes LIKE :eventType || ',' || '%' OR " +
           "ws.eventTypes LIKE '%,' || :eventType || ',' || '%' OR " +
           "ws.eventTypes LIKE '%,' || :eventType)")
    List<WebhookSubscription> findByTeamIdAndEventTypesContainingAndActiveTrue(@Param("teamId") UUID teamId, @Param("eventType") String eventType);
}
