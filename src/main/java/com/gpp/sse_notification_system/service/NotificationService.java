package com.gpp.sse_notification_system.service;

import com.gpp.sse_notification_system.model.Event;
import com.gpp.sse_notification_system.model.UserSubscription;
import com.gpp.sse_notification_system.model.UserSubscriptionId;
import com.gpp.sse_notification_system.repository.EventRepository;
import com.gpp.sse_notification_system.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EventRepository eventRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SseConnectionManager connectionManager;

    @Transactional
    public Event publishEvent(String channel, String eventType, Map<String, Object> payload) {
        Event event = new Event();
        event.setChannel(channel);
        event.setEventType(eventType);
        event.setPayload(payload);

        Event savedEvent = eventRepository.save(event);
        log.info("Published event id {} to channel {}", savedEvent.getId(), channel);

        connectionManager.broadcast(savedEvent);
        return savedEvent;
    }

    @Transactional
    public UserSubscription subscribeUserToChannel(Integer userId, String channel) {
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setChannel(channel);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribeUserFromChannel(Integer userId, String channel) {
        UserSubscriptionId id = new UserSubscriptionId(userId, channel);
        subscriptionRepository.deleteById(id);
    }

    public Set<String> getValidUserChannels(Integer userId, List<String> requestedChannels) {
        List<UserSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        Set<String> subscribedChannels = subscriptions.stream()
                .map(UserSubscription::getChannel)
                .collect(Collectors.toSet());

        return requestedChannels.stream()
                .filter(subscribedChannels::contains)
                .collect(Collectors.toSet());
    }

    public List<Event> getMissedEvents(Set<String> channels, Long lastEventId) {
        return eventRepository.findByChannelInAndIdGreaterThanOrderByIdAsc(List.copyOf(channels), lastEventId);
    }
}
