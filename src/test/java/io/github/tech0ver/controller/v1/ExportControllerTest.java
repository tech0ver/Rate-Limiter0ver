package io.github.tech0ver.controller.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
class ExportControllerTest {

    final String baseUrl = "/api/v1/exports";

    @Autowired
    WebApplicationContext ctx;
    MockMvcTester mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcTester.from(ctx);
    }

    @Test
    void should_create_job_and_return_status() {
        assertThat(mvc.post().uri(baseUrl).header("X-API-Key", "t0-NJS-202"))
                .hasStatus(HttpStatus.ACCEPTED)
                .matches(header().string(HttpHeaders.LOCATION, matchesPattern(".*" + baseUrl + "/0$")));
        var result = mvc.get().uri(baseUrl + "/0").header("X-API-Key", "t0-NJS-202").exchange();
        assertThat(result).hasStatusOk().hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.id").asNumber().isEqualTo(0);
        assertThat(result).bodyJson().extractingPath("$.createdAt").asString().isNotEmpty();
        assertThat(result).bodyJson().extractingPath("$.modifiedAt").asString().isNotEmpty();
        assertThat(result).bodyJson().extractingPath("$.status").asString().isNotEmpty();
        assertThat(result).bodyJson().extractingPath("$.message").asString().isNotEmpty();
    }

    @Test
    void shouldNot_find_job() {
        assertThat(mvc.get().uri(baseUrl + "/987").header("X-API-Key", "t0-JNF-404"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasBodyTextEqualTo("Job not found by id: 987");
    }

}