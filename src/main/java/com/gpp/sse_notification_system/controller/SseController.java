package com.gpp.sse_notification_system.controller;

import com.gpp.sse_notification_system.model.Event;
import com.gpp.sse_notification_system.service.NotificationService;
import com.gpp.sse_notification_system.service.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

        // Define timeout: 0 means no timeout (rely on underlying network closing or our keep-alives)
        SseEmitter emitter = new SseEmitter(0L);

        if (validChannels.isEmpty()) {
            emitter.complete();
            return emitter;
        }

        connectionManager.addConnection(emitter, validChannels);

        // Handle Event Replay logic (Last-Event-ID)
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
                    return emitter; // Prevent sending more if IO fails
                }
            }
        }

        return emitter;
    }
}
