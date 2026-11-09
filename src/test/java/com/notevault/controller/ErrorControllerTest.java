package com.notevault.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Browser HTML 404 request forwards to /error/404.html")
    void shouldForwardBrowserTo404Html() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 404)
                        .requestAttr("jakarta.servlet.error.request_uri", "/unknown-page")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(forwardedUrl("/error/404.html"));
    }

    @Test
    @DisplayName("API JSON 404 request returns structured ErrorResponse JSON")
    void shouldReturnJsonForApiErrors() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 404)
                        .requestAttr("jakarta.servlet.error.request_uri", "/api/v1/unknown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}
