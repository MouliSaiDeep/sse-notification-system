package com.gpp.sse_notification_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotBlank(message = "Channel cannot be empty")
    private String channel;
}
