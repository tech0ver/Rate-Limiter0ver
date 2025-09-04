package io.github.tech0ver.demo.mapper;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.payload.EventResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface EventResponseMapper extends ResponseMapper<Event, EventResponse> {

    @Override
    EventResponse mapDomain2Response(Event event);

}
