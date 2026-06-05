package io.github.tech0ver.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerTest {

    final String baseUrl = "/api/v1/statuses";

    @Autowired
    MockMvc mvc;

    @Test
    void should_return_status() throws Exception {
        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.now").isNotEmpty())
                .andExpect(jsonPath("$.uptimeSeconds").isNumber());
    }

    @Test
    void should_return_429_for_sameIp() throws Exception {
        mvc.perform(get(baseUrl).with(request -> { request.setRemoteAddr("192.168.1.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Remaining", "1"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "fixed-window; limit=2; window=PT30S"));
        mvc.perform(get(baseUrl).with(request -> { request.setRemoteAddr("192.168.1.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "fixed-window; limit=2; window=PT30S"));
        mvc.perform(get(baseUrl).with(request -> { request.setRemoteAddr("192.168.1.1"); return request; }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", notNullValue()))
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("RateLimit-Limit", "2;w=30"))
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Policy", "fixed-window; limit=2; window=PT30S"))
                .andExpect(header().string("RateLimit-Reset", notNullValue()))
                .andExpect(header().string("X-RateLimit-Reset", notNullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{\"error\":\"Too many requests for getting status.\"}"));
    }

}