package com.gpp.sse_notification_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionId implements Serializable {
    private Integer userId;
    private String channel;
}
