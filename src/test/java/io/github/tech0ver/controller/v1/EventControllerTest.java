package io.github.tech0ver.controller.v1;

import io.github.tech0ver.domain.Event;
import io.github.tech0ver.payload.v1.NewEventRequest;
import io.github.tech0ver.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
class EventControllerTest {

    final String baseUrl = "/api/v1/events";

    @Autowired
    WebApplicationContext ctx;
    MockMvcTester mvc;
    @Autowired
    JsonMapper mapper;

    @Autowired
    EventService eventService;

    @BeforeEach
    void setUp() {
        mvc = MockMvcTester.from(ctx);
    }

    @Test
    void should_create_newEvents() {
        String body = mapper.writeValueAsString(List.of(
                new NewEventRequest("ignored", Instant.now())
        ));
        assertThat(mvc.post().uri(baseUrl)
                .header("X-API-Key", "t0-CRT-201")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED)
                .matches(header().string(HttpHeaders.LOCATION, endsWith(baseUrl)));
    }

    @Test
    void should_search_events() {
        String value = "event";
        Instant createdAt = Instant.now();
        eventService.addAll("t0-SCH-200", List.of(new Event(value, createdAt)));
        var result = mvc.get().uri(baseUrl).header("X-API-Key", "t0-SCH-200").exchange();
        assertThat(result).hasStatusOk().hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.[0].value").asString().isEqualTo(value);
        assertThat(result).bodyJson().extractingPath("$.[0].createdAt").asString().isEqualTo(createdAt.toString());
    }

}