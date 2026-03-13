package com.gpp.sse_notification_system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "user_subscriptions")
@IdClass(UserSubscriptionId.class)
public class UserSubscription {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "channel")
    private String channel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
