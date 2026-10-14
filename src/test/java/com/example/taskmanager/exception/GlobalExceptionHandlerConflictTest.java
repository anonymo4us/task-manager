package com.example.taskmanager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerConflictTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn409WithErrorAndMessageWhenConflictIsThrown() throws Exception {
        mockMvc.perform(get("/test/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Team 42 cannot be deleted: users or projects reference it"));
    }

    @Test
    void shouldReturn409ForAnyConflictMessage() throws Exception {
        mockMvc.perform(get("/test/conflict-empty").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(""));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/conflict")
        public void conflict() {
            throw new ResourceConflictException(
                    "Team 42 cannot be deleted: users or projects reference it");
        }

        @GetMapping("/test/conflict-empty")
        public void conflictEmpty() {
            throw new ResourceConflictException("");
        }
    }
}