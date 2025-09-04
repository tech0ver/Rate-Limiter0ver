package io.github.tech0ver.demo.controller;

import io.github.tech0ver.demo.domain.Status;
import io.github.tech0ver.demo.mapper.StatusResponseMapper;
import io.github.tech0ver.demo.payload.StatusResponse;
import io.github.tech0ver.demo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statuses")
public class StatusController {

    private final StatusService statusService;
    private final StatusResponseMapper statusResponseMapper;

    @GetMapping
    public ResponseEntity<?> getStatus() {
        Status status = statusService.getStatus();
        StatusResponse response = statusResponseMapper.mapDomain2Response(status);
        return ResponseEntity.ok(response);
    }

}
