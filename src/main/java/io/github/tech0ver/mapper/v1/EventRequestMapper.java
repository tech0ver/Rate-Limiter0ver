package io.github.tech0ver.mapper.v1;

import io.github.tech0ver.config.MapStructConfig;
import io.github.tech0ver.domain.Event;
import io.github.tech0ver.mapper.RequestMapper;
import io.github.tech0ver.payload.v1.NewEventRequest;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface EventRequestMapper extends RequestMapper<NewEventRequest, Event> {

    @Override
    Event mapRequest2Domain(NewEventRequest newEventRequest);

}
