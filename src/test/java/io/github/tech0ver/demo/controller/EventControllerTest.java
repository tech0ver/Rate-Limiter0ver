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
        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl)));
    }

    @Test
    void should_search_events() throws Exception {
        String value = "event";
        Instant createdAt = Instant.now();
        eventService.addAll(List.of(new Event(value, createdAt)));
        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].value").value(value))
                .andExpect(jsonPath("$.[0].createdAt").value(createdAt.toString()));
    }

}