package io.github.tech0ver.controller.v1;

import io.github.tech0ver.annotation.ValidApiKey;
import io.github.tech0ver.domain.Event;
import io.github.tech0ver.domain.EventSearchCondition;
import io.github.tech0ver.mapper.v1.EventRequestMapper;
import io.github.tech0ver.mapper.v1.EventResponseMapper;
import io.github.tech0ver.payload.v1.EventResponse;
import io.github.tech0ver.payload.v1.NewEventRequest;
import io.github.tech0ver.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
@Validated
public class EventController {

    private final EventService eventService;
    private final EventRequestMapper eventRequestMapper;
    private final EventResponseMapper eventResponseMapper;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("X-API-Key") @ValidApiKey String apiKey,
            @RequestBody List<NewEventRequest> request
    ) {
        List<Event> events = request.stream()
                .map(eventRequestMapper::mapRequest2Domain)
                .toList();
        eventService.addAll(apiKey, events);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<?> search(
            @RequestHeader("X-API-Key") @ValidApiKey String apiKey,
            @RequestParam(name = "from", required = false)
            Instant from,
            @RequestParam(name = "to", required = false)
            Instant to
    ) {
        EventSearchCondition condition = new EventSearchCondition(from, to);
        List<Event> events = eventService.search(apiKey, condition);
        List<EventResponse> response = events.stream()
                .map(eventResponseMapper::mapDomain2Response)
                .toList();
        return ResponseEntity.ok(response);
    }

}
