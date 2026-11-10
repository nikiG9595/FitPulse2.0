package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void fitPulseExceptionUsesCustomErrorPage() throws Exception {
        mockMvc.perform(get("/test/fitpulse"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/custom-error"))
                .andExpect(model().attribute("message", "Ресурсът не е намерен."));
    }

    @Test
    void invalidUuidUsesSafeBadRequestPage() throws Exception {
        mockMvc.perform(get("/test/uuid/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/custom-error"))
                .andExpect(model().attribute("message",
                        "The requested identifier is invalid."));
    }

    @Test
    void dataIntegrityViolationUsesSafeConflictPage() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error/custom-error"))
                .andExpect(model().attribute("message",
                        "This item cannot be changed because it is still in use."));
    }

    @Controller
    static class ExceptionTestController {

        @GetMapping("/test/fitpulse")
        String fitPulseException() {
            throw new FitPulseException("Ресурсът не е намерен.");
        }

        @GetMapping("/test/uuid/{id}")
        String uuid(@PathVariable UUID id) {
            return "dashboard";
        }

        @GetMapping("/test/data-integrity")
        String dataIntegrityViolation() {
            throw new DataIntegrityViolationException(
                    "foreign key constraint details");
        }
    }
}
