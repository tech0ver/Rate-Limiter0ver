package io.github.tech0ver.demo.controller;

import io.github.tech0ver.demo.annotation.ValidApiKey;
import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.exception.JobFailedException;
import io.github.tech0ver.demo.exception.JobNotFoundException;
import io.github.tech0ver.demo.exception.JobNotReadyException;
import io.github.tech0ver.demo.mapper.ExportJobResponseMapper;
import io.github.tech0ver.demo.payload.ExportJobResponse;
import io.github.tech0ver.demo.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService exportService;
    private final ExportJobResponseMapper exportJobResponseMapper;

    @PostMapping
    public ResponseEntity<?> createJob(
            @RequestHeader("X-API-Key") @ValidApiKey String apiKey
    ) {
        long jobId = exportService.createJob(apiKey);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{jobId}")
                .buildAndExpand(jobId)
                .toUri();
        return ResponseEntity
                .accepted().
                location(location)
                .build();
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(
            @RequestHeader("X-API-Key") @ValidApiKey String apiKey,
            @PathVariable long jobId
    ) throws JobNotFoundException {
        ExportJob.Snapshot job = exportService.getJobSnapshot(apiKey, jobId);
        ExportJobResponse response = exportJobResponseMapper.mapDomain2Response(job);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}/files")
    public ResponseEntity<?> getJobFiles(
            @RequestHeader("X-API-Key") @ValidApiKey String apiKey,
            @PathVariable long jobId
    ) throws JobNotFoundException {
        try {
            ExportJob.File file = exportService.getJobFile(apiKey, jobId);
            Path path = file.path();
            String baseName = path.getFileName().toString();
            String encoded = URLEncoder.encode(baseName, StandardCharsets.UTF_8);
            String cd = "attachment; filename=\"" + baseName + "\"; filename*=UTF-8''" + encoded;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, cd);
            headers.setContentType(MediaType.parseMediaType(file.mediaType()));
            headers.setLastModified(file.modifiedAt());
            headers.setETag(String.format("W/\"%d-%d\"", file.size(), file.modifiedAt().toEpochMilli()));
            headers.setCacheControl(CacheControl.noStore().getHeaderValue());
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new FileSystemResource(path));
        } catch (JobNotReadyException e) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/{jobId}")
                    .buildAndExpand(jobId)
                    .toUri();
            return ResponseEntity
                    .status(HttpStatus.TOO_EARLY)
                    .location(location)
                    .build();
        } catch (JobFailedException e) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/{jobId}")
                    .buildAndExpand(jobId)
                    .toUri();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .location(location)
                    .build();
        }
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<String> handleJobNotFoundException(JobNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

}
