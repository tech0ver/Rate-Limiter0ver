package io.github.tech0ver.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.payload.NewEventRequest;
import io.github.tech0ver.demo.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    final String baseUrl = "/api/v1/events";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    EventService eventService;

    @Test
    void should_create_newEvents() throws Exception {
        String body = mapper.writeValueAsString(List.of(
                new NewEventRequest("ignored", Instant.now())
        ));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-CRT-201")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl)));
    }

    @Test
    void should_return_429_for_creating_newEvents() throws Exception {
        String body = mapper.writeValueAsString(List.of(
                new NewEventRequest("ignored", Instant.now())
        ));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-CRT-429")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl)))
                .andExpect(header().string("RateLimit-Remaining", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "token-bucket; capacity=2; refill=PT30S; tokens=1"));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-CRT-429")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl)))
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "token-bucket; capacity=2; refill=PT30S; tokens=1"));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-CRT-429")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "token-bucket; capacity=2; refill=PT30S; tokens=1"))
                .andExpect(header().string("RateLimit-Reset", notNullValue()))
                .andExpect(header().string("X-RateLimit-Reset", notNullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"error\":\"Too many requests for creating events.\"}"));
    }

    @Test
    void should_search_events() throws Exception {
        String value = "event";
        Instant createdAt = Instant.now();
        eventService.addAll("t0-SCH-200", List.of(new Event(value, createdAt)));
        mvc.perform(get(baseUrl).header("X-API-Key", "t0-SCH-200"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].value").value(value))
                .andExpect(jsonPath("$.[0].createdAt").value(createdAt.toString()));
    }

    @Test
    void should_return_429_for_searching_events() throws Exception {
        String value = "event";
        Instant createdAt = Instant.now();
        eventService.addAll("t0-SCH-429", List.of(new Event(value, createdAt)));
        mvc.perform(get(baseUrl).header("X-API-Key", "t0-SCH-429"))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Remaining", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "sliding-linear; limit=2; window=PT30S"));
        mvc.perform(get(baseUrl).header("X-API-Key", "t0-SCH-429"))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "sliding-linear; limit=2; window=PT30S"));
        mvc.perform(get(baseUrl).header("X-API-Key", "t0-SCH-429"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "sliding-linear; limit=2; window=PT30S"))
                .andExpect(header().string("RateLimit-Reset", notNullValue()))
                .andExpect(header().string("X-RateLimit-Reset", notNullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"error\":\"Too many requests for searching events.\"}"));
    }

}