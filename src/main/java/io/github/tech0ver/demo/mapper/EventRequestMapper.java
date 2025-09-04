package io.github.tech0ver.demo.mapper;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.payload.NewEventRequest;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface EventRequestMapper extends RequestMapper<NewEventRequest, Event> {

    @Override
    Event mapRequest2Domain(NewEventRequest newEventRequest);

}
