package com.gpp.sse_notification_system.controller;

import com.gpp.sse_notification_system.model.Event;
import com.gpp.sse_notification_system.service.NotificationService;
import com.gpp.sse_notification_system.service.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class SseController {
    private final NotificationService notificationService;
    private final SseConnectionManager connectionManager;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(
            @RequestParam Integer userId,
            @RequestParam String channels,
            @RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId) {

        List<String> requestedChannels = Arrays.stream(channels.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        Set<String> validChannels = notificationService.getValidUserChannels(userId, requestedChannels);

        if (validChannels.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User is not subscribed to any of the requested channels");
        }

        // Timeout 0 = no timeout; rely on heartbeats to keep alive
        SseEmitter emitter = new SseEmitter(0L);

        connectionManager.addConnection(emitter, validChannels);

        // Event Replay: stream missed events before going live
        if (lastEventId != null) {
            List<Event> missedEvents = notificationService.getMissedEvents(validChannels, lastEventId);
            for (Event event : missedEvents) {
                try {
                    emitter.send(SseEmitter.event()
                            .id(event.getId().toString())
                            .name(event.getEventType())
                            .data(event.getPayload()));
                } catch (IOException e) {
                    log.error("Error sending missed event during replay", e);
                    emitter.completeWithError(e);
                    return emitter;
                }
            }
        }

        return emitter;
    }

    @GetMapping("/active-connections")
    public ResponseEntity<Map<String, Integer>> getActiveConnections() {
        return ResponseEntity.ok(Map.of("activeConnections", connectionManager.getActiveConnectionCount()));
    }
}

