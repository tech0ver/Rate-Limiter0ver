package io.github.tech0ver.controller.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StatusControllerTest {

    final String baseUrl = "/api/v1/statuses";

    @Autowired
    WebApplicationContext ctx;
    MockMvcTester mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcTester.from(ctx);
    }

    @Test
    void should_return_status() {
        var result = mvc.get().uri(baseUrl).exchange();
        assertThat(result).hasStatusOk().hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.now").asString().isNotEmpty();
        assertThat(result).bodyJson().extractingPath("$.uptimeSeconds").asNumber().isNotNull();
    }

}