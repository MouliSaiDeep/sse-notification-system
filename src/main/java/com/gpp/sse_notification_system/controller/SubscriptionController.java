package com.gpp.sse_notification_system.controller;

import com.gpp.sse_notification_system.dto.SubscriptionRequest;
import com.gpp.sse_notification_system.dto.SubscriptionResponse;
import com.gpp.sse_notification_system.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/channels")
@RequiredArgsConstructor
public class SubscriptionController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listChannels(@RequestParam Integer userId) {
        List<String> channels = notificationService.getUserChannels(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("channels", channels);
        return ResponseEntity.ok(response);
    }

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
