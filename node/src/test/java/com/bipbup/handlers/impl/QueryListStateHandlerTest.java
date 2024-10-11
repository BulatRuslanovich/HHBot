package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_OUTPUT;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QueryListStateHandlerTest {

    @Mock
    private UserService userService;

    @Mock
    private ConfigService configService;

    @Mock
    private Decoder decoder;

    @InjectMocks
    private QueryListStateHandler queryListStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    @Test
    @DisplayName("Should process query command and return config output")
    void testProcessQueryCommand_Success() {
        String input = Prefix.QUERY + " 1";
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");
        config.setQueryText("LOLOLO");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getById(configId)).thenReturn(Optional.of(config));

        String result = queryListStateHandler.process(appUser, input);

        verify(userService).saveUserState(appUser.getTelegramId(), QUERY_MENU_STATE);
        assertEquals(String.format(QUERY_OUTPUT.getTemplate(), config.getConfigName(), config.getQueryText()), result);
    }

    @Test
    @DisplayName("Should return not found message when config does not exist")
    void testProcessQueryCommand_ConfigNotFound() {
        String input = Prefix.QUERY + " 1";
        long configId = 1L;

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getById(configId)).thenReturn(Optional.empty());

        String result = queryListStateHandler.process(appUser, input);

        verify(userService, never()).saveUserState(anyLong(), any());
        assertEquals(CONFIG_NOT_FOUND.getTemplate(), result);
    }

    @Test
    @DisplayName("Should return empty string for non-query input")
    void testProcessNonQueryInput() {
        String input = "some other command";

        String result = queryListStateHandler.process(appUser, input);

        assertEquals("", result);
    }
}
