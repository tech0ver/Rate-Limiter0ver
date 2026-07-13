package io.github.tech0ver.demo.mapper.v1;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.mapper.ResponseMapper;
import io.github.tech0ver.demo.payload.v1.ExportJobResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface ExportJobResponseMapper extends ResponseMapper<ExportJob.Snapshot, ExportJobResponse> {

    @Override
    ExportJobResponse mapDomain2Response(ExportJob.Snapshot exportJob);

}
