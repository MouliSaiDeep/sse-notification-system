package com.gpp.sse_notification_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class PublishRequest {
    @NotBlank(message = "Channel cannot be empty")
    private String channel;

    @NotBlank(message = "Event type cannot be empty")
    private String eventType;

    @NotNull(message = "Payload cannot be null")
    private Map<String, Object> payload;
}
