package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.CommandMessageConstants.Prefix;
import com.bipbup.utils.Decoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_DELETED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.Prefix.DELETE_CONFIRM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueryDeleteStateHandlerTest {
    @Mock
    private UserService userService;

    @Mock
    private ConfigService configService;

    @Mock
    private QueryListStateHandler queryListStateHandler;

    @Mock
    private Decoder decoder;

    @InjectMocks
    private QueryDeleteStateHandler queryDeleteStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    @Test
    @DisplayName("Should process delete confirm command and delete configuration")
    void testProcessDeleteConfirmCommand_Success() {
        String input = Prefix.DELETE_CONFIRM + " 1"; // Example input
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getById(configId)).thenReturn(Optional.of(config));

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(configService).delete(config);
        verify(userService).saveUserState(appUser.getTelegramId(), QUERY_LIST_STATE);
        assertEquals(String.format(CONFIG_DELETED.getTemplate(), config.getConfigName()) + "\n" + USER_QUERIES.getTemplate(), result);
    }

    @Test
    @DisplayName("Should return not found message when config does not exist")
    void testProcessDeleteConfirmCommand_ConfigNotFound() {
        String input = DELETE_CONFIRM + " 1"; // Example input
        long configId = 1L;

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getById(configId)).thenReturn(Optional.empty());

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(CONFIG_NOT_FOUND.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process delete cancel command")
    void testProcessDeleteCancelCommand() {
        String input = Prefix.QUERY + " some input"; // Example input

        when(queryListStateHandler.process(appUser, input)).thenReturn("Query List");

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(userService).saveUserState(appUser.getTelegramId(), QUERY_LIST_STATE);
        assertEquals("Query List", result);
    }
}