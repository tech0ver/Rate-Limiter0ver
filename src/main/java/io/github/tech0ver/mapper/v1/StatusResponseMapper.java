package io.github.tech0ver.mapper.v1;

import io.github.tech0ver.config.SpringMapStructConfig;
import io.github.tech0ver.domain.Status;
import io.github.tech0ver.mapper.ResponseMapper;
import io.github.tech0ver.payload.v1.StatusResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface StatusResponseMapper extends ResponseMapper<Status, StatusResponse> {

    @Override
    StatusResponse mapDomain2Response(Status status);

}
