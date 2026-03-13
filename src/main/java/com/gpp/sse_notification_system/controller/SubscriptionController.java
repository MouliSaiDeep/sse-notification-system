package com.gpp.sse_notification_system.controller;

import com.gpp.sse_notification_system.dto.SubscriptionRequest;
import com.gpp.sse_notification_system.dto.SubscriptionResponse;
import com.gpp.sse_notification_system.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/channels")
@RequiredArgsConstructor
public class SubscriptionController {
    private final NotificationService notificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        notificationService.subscribeUserToChannel(request.getUserId(), request.getChannel());

        SubscriptionResponse response = new SubscriptionResponse(
                "subscribed", request.getUserId(), request.getChannel()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<SubscriptionResponse> unsubscribe(@Valid @RequestBody SubscriptionRequest request) {
        notificationService.unsubscribeUserFromChannel(request.getUserId(), request.getChannel());

        SubscriptionResponse response = new SubscriptionResponse(
                "unsubscribed", request.getUserId(), request.getChannel()
        );
        return ResponseEntity.ok(response);
    }
}
