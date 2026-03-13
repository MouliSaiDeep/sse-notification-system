package com.gpp.sse_notification_system.controller;

import com.gpp.sse_notification_system.dto.PublishRequest;
import com.gpp.sse_notification_system.model.Event;
import com.gpp.sse_notification_system.repository.EventRepository;
import com.gpp.sse_notification_system.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final NotificationService notificationService;
    private final EventRepository eventRepository;

    @PostMapping("/publish")
    public ResponseEntity<Void> publishEvent(@Valid @RequestBody PublishRequest request) {
        notificationService.publishEvent(request.getChannel(), request.getEventType(), request.getPayload());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam String channel,
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "50") int limit) {

        Page<Event> eventsPage;
        if (afterId != null) {
            eventsPage = eventRepository.findByChannelAndIdGreaterThanOrderByIdAsc(channel, afterId, PageRequest.of(0, limit));
        } else {
            eventsPage = eventRepository.findByChannelOrderByIdAsc(channel, PageRequest.of(0, limit));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("events", eventsPage.getContent());
        return ResponseEntity.ok(response);
    }
}
