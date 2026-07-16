package io.github.tech0ver.service;

import io.github.tech0ver.domain.Event;
import io.github.tech0ver.domain.EventSearchCondition;

import java.util.List;

public interface EventService {

    List<Event> search(String apiKey, EventSearchCondition condition);

    void addAll(String apiKey, List<Event> newEvents);

}
