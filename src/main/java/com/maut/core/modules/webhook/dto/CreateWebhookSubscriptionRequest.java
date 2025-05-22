package com.maut.core.modules.webhook.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateWebhookSubscriptionRequest {
    @NotBlank(message = "Target URL cannot be blank")
    @URL(message = "Target URL must be a valid HTTP/HTTPS URL")
    @Size(max = 2048, message = "Target URL cannot exceed 2048 characters")
    private String targetUrl;

    @NotEmpty(message = "Event types cannot be empty")
    private List<String> eventTypes;
}
