package io.github.tech0ver.demo.mapper.v1;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.Status;
import io.github.tech0ver.demo.mapper.ResponseMapper;
import io.github.tech0ver.demo.payload.v1.StatusResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface StatusResponseMapper extends ResponseMapper<Status, StatusResponse> {

    @Override
    StatusResponse mapDomain2Response(Status status);

}
