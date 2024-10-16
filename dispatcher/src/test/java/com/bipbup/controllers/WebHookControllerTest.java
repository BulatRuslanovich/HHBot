package com.bipbup.controllers;

import com.bipbup.exception.NullUpdateException;
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
    void testOnUpdateReceived_Success() throws Exception {
        // Arrange
        Update update = new Update();
        processor.processUpdate(update);

        // Act
        mockMvc.perform(post("/callback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                // Assert
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update processing fails")
    void testOnUpdateReceived_Failure() throws Exception {
        // Arrange
        Update update = new Update();
        processor.processUpdate(update);
        when(processor).thenThrow(NullUpdateException.class);

        // Act
        mockMvc.perform(post("/callback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to process the update."));
    }
}