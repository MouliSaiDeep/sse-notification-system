package com.gpp.sse_notification_system.service;

import com.gpp.sse_notification_system.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseConnectionManager {
    private final ConcurrentHashMap<String, ClientConnection> activeConnections = new ConcurrentHashMap<>();

    public String addConnection(SseEmitter emitter, Set<String> channels) {
        String connectionId = UUID.randomUUID().toString();
        ClientConnection connection = new ClientConnection(connectionId, emitter, channels);
        activeConnections.put(connectionId, connection);
        
        log.info("Client connected: {} for channels {}", connectionId, channels);
        

        emitter.onCompletion(() -> removeConnection(connectionId));
        emitter.onTimeout(() -> removeConnection(connectionId));
        emitter.onError((e) -> removeConnection(connectionId));

        return connectionId;
    }

    public void removeConnection(String connectionId) {
        if (activeConnections.remove(connectionId) != null) {
            log.info("Client disconnected: {}", connectionId);
        }
    }

    public void broadcast(Event event) {
        String targetChannel = event.getChannel();
        activeConnections.forEach((id, conn) -> {
            if (conn.getSubscribedChannels().contains(targetChannel)) {
                try {
                    conn.getEmitter().send(SseEmitter.event()
                            .id(event.getId().toString())
                            .name(event.getEventType())
                            .data(event.getPayload()));
                } catch (IOException e) {
                    log.error("Failed to send event to connection {}. Removing.", id);
                    conn.getEmitter().completeWithError(e);
                    removeConnection(id);
                }
            }
        });
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeats() {
        activeConnections.forEach((id, conn) -> {
            try {
                conn.getEmitter().send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException e) {
                log.error("Failed to send heartbeat to connection {}. Removing.", id);
                conn.getEmitter().completeWithError(e);
                removeConnection(id);
            }
        });
    }

    public int getActiveConnectionCount() {
        return activeConnections.size();
    }
}
