package io.github.tech0ver.mapper.v1;

import io.github.tech0ver.config.SpringMapStructConfig;
import io.github.tech0ver.domain.ExportJob;
import io.github.tech0ver.mapper.ResponseMapper;
import io.github.tech0ver.payload.v1.ExportJobResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface ExportJobResponseMapper extends ResponseMapper<ExportJob.Snapshot, ExportJobResponse> {

    @Override
    ExportJobResponse mapDomain2Response(ExportJob.Snapshot exportJob);

}
