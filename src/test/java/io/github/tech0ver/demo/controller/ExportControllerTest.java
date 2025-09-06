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
        // create job
        mvc.perform(post(baseUrl))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(baseUrl + "/0")));
        // get status
        mvc.perform(get(baseUrl + "/0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.modifiedAt").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void should_notFound_job() throws Exception {
        mvc.perform(get(baseUrl + "/123"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Job not found by id: 123"));
    }

}