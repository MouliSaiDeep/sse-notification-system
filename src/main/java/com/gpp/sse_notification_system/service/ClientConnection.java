package com.gpp.sse_notification_system.service;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@Getter
public class ClientConnection {
    private final String connectionId;
    private final SseEmitter emitter;
    private final Set<String> subscribedChannels;

    public ClientConnection(String connectionId, SseEmitter emitter, Set<String> subscribedChannels) {
        this.connectionId = connectionId;
        this.emitter = emitter;
        this.subscribedChannels = subscribedChannels;
    }
}