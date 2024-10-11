package com.bipbup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebHookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UpdateProcessor processor;

    @InjectMocks
    private WebHookController webHookController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(webHookController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should return 200 OK when update is processed successfully")
    public void testOnUpdateReceived_Success() throws Exception {
        // Arrange
        Update update = new Update();
        when(processor.processUpdate(update)).thenReturn(true);

        // Act
        mockMvc.perform(post("/callback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                // Assert
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update processing fails")
    public void testOnUpdateReceived_Failure() throws Exception {
        // Arrange
        Update update = new Update();
        when(processor.processUpdate(update)).thenReturn(false);

        // Act
        mockMvc.perform(post("/callback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to process the update."));
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when an exception occurs during processing")
    public void testOnUpdateReceived_Exception() throws Exception {
        // Arrange
        Update update = new Update();
        when(processor.processUpdate(update)).thenThrow(new RuntimeException("Processing error"));

        // Act
        mockMvc.perform(post("/callback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                // Assert
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Internal Server Error: Processing error")));
    }
}