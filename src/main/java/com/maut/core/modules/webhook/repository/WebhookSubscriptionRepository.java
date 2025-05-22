package com.maut.core.modules.webhook.repository;

import com.maut.core.modules.webhook.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    List<WebhookSubscription> findByClientApplicationId(UUID clientApplicationId);
    Optional<WebhookSubscription> findByIdAndClientApplicationId(UUID id, UUID clientApplicationId);
    boolean existsByClientApplicationIdAndTargetUrlAndActiveTrue(UUID clientApplicationId, String targetUrl);
}
