package io.github.tech0ver.controller.v1;

import io.github.tech0ver.domain.Status;
import io.github.tech0ver.mapper.v1.StatusResponseMapper;
import io.github.tech0ver.payload.v1.StatusResponse;
import io.github.tech0ver.service.StatusService;
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
