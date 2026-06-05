package io.github.tech0ver.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tech0ver.demo.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    final String baseUrl = "/api/v1/exports";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    EventService eventService;

    @Test
    void should_create_job_and_return_status() throws Exception {
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-NJS-202"))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl + "/0")));
        mvc.perform(get(baseUrl + "/0").header("X-API-Key", "t0-NJS-202"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.modifiedAt").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldNot_find_job() throws Exception {
        mvc.perform(get(baseUrl + "/987").header("X-API-Key", "t0-JNF-404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Job not found by id: 987"));
    }


    @Test
    void should_return_429_for_creating_job() throws Exception {
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-NJB-429"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("RateLimit-Remaining", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "leaky-bucket; capacity=2; interval=PT30S"));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-NJB-429"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "leaky-bucket; capacity=2; interval=PT30S"));
        mvc.perform(post(baseUrl).header("X-API-Key", "t0-NJB-429"))
d                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "leaky-bucket; capacity=2; interval=PT30S"))
                .andExpect(header().string("RateLimit-Reset", notNullValue()))
                .andExpect(header().string("X-RateLimit-Reset", notNullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"error\":\"Too many requests for creating export jobs.\"}"));
    }

}