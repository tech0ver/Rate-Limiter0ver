package io.github.tech0ver.demo.mapper;

import io.github.tech0ver.demo.config.SpringMapStructConfig;
import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.payload.ExportJobResponse;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapStructConfig.class)
public interface ExportJobResponseMapper extends ResponseMapper<ExportJob.Snapshot, ExportJobResponse> {

    @Override
    ExportJobResponse mapDomain2Response(ExportJob.Snapshot exportJob);

}
