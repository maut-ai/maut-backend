package com.maut.core.modules.webhook.dto;

import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UpdateWebhookSubscriptionRequest {
    @URL(message = "Target URL must be a valid HTTP/HTTPS URL")
    @Size(max = 2048, message = "Target URL cannot exceed 2048 characters")
    private String targetUrl; // Optional

    private List<String> eventTypes; // Optional

    private Boolean isActive; // Optional
}
