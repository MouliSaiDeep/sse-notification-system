package com.gpp.sse_notification_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionResponse {
    private String status;
    private Integer userId;
    private String channel;
}
