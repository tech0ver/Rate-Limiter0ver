package io.github.tech0ver.mapper.v1;

import io.github.tech0ver.config.MapStructConfig;
import io.github.tech0ver.domain.Event;
import io.github.tech0ver.mapper.ResponseMapper;
import io.github.tech0ver.payload.v1.EventResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface EventResponseMapper extends ResponseMapper<Event, EventResponse> {

    @Override
    EventResponse mapDomain2Response(Event event);

}
