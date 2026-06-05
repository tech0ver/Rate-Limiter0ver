package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.domain.EventSearchCondition;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class InMemoryEventService implements EventService {

    private final Map<String, NavigableSet<Event>> eventsByApiKey;

    public InMemoryEventService() {
        this.eventsByApiKey = new ConcurrentHashMap<>();
    }

    @Override
    public List<Event> search(String apiKey, EventSearchCondition condition) {
        NavigableSet<Event> events = eventsByApiKey.get(apiKey);
        if (events == null || events.isEmpty()) return List.of();
        Instant from = condition.from() != null ? condition.from() : Instant.MIN;
        Instant to = condition.to() != null ? condition.to() : Instant.MAX;
        return events.subSet(
                new Event(null, from),
                true,
                new Event(null, to),
                true
        ).stream().toList();
    }

    @Override
    public void addAll(String apiKey, List<Event> newEvents) {
        eventsByApiKey.computeIfAbsent(apiKey, k -> new ConcurrentSkipListSet<>(
                Comparator.comparing(Event::createdAt).thenComparing(Event::value))
        ).addAll(newEvents);
    }

}
