package io.github.tech0ver.demo.mapper.v1;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.mapper.ResponseMapper;
import io.github.tech0ver.demo.payload.v1.EventResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface EventResponseMapper extends ResponseMapper<Event, EventResponse> {

    @Override
    EventResponse mapDomain2Response(Event event);

}
