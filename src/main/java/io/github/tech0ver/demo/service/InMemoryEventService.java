package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.domain.EventSearchCondition;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class InMemoryEventService implements EventService {

    private final NavigableSet<Event> events;

    public InMemoryEventService() {
        this.events = new ConcurrentSkipListSet<>(
                Comparator.comparing(Event::createdAt)
                        .thenComparing(Event::value)
        );
    }

    @Override
    public List<Event> search(EventSearchCondition condition) {
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
    public void addAll(List<Event> newEvents) {
        events.addAll(newEvents);
    }

}
