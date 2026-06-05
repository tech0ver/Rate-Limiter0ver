package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.domain.EventSearchCondition;

import java.util.List;

public interface EventService {

    List<Event> search(String apiKey, EventSearchCondition condition);

    void addAll(String apiKey, List<Event> newEvents);

}
