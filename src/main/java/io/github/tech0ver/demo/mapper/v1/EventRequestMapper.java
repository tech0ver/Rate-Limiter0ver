package io.github.tech0ver.demo.mapper.v1;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.mapper.RequestMapper;
import io.github.tech0ver.demo.payload.v1.NewEventRequest;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface EventRequestMapper extends RequestMapper<NewEventRequest, Event> {

    @Override
    Event mapRequest2Domain(NewEventRequest newEventRequest);

}
