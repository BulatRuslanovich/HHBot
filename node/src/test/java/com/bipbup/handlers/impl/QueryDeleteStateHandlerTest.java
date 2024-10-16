package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import com.bipbup.handlers.impl.callback.QueryDeleteStateHandler;
import com.bipbup.handlers.impl.callback.QueryListStateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_DELETED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import com.bipbup.utils.CommandMessageConstants.Prefix;
import static com.bipbup.utils.CommandMessageConstants.Prefix.DELETE_CONFIRM;
import com.bipbup.utils.Decoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class QueryDeleteStateHandlerTest {

    @Mock
    private ConfigService configService;

    @Mock
    private UserStateCacheService userStateCacheService;

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
    @DisplayName("Should process delete confirm command and delete configuration (when 1 config)")
    void testProcessDeleteConfirmCommandWhenOneConfig_Success() {
        String input = Prefix.DELETE_CONFIRM + " 1"; // Example input
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.of(config));
        when(configService.countOfConfigs(appUser)).thenReturn(0L);

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(configService).deleteConfig(config);
        assertEquals(String.format(CONFIG_DELETED.toString(), config.getConfigName()), result);
    }

    @Test
    @DisplayName("Should process delete confirm command and delete configuration (when more 1 config)")
    void testProcessDeleteConfirmCommandWhenManyConfig_Success() {
        String input = Prefix.DELETE_CONFIRM + " 1"; // Example input
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.of(config));
        when(configService.countOfConfigs(appUser)).thenReturn(1L);

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(configService).deleteConfig(config);
        verify(userStateCacheService).putUserState(appUser.getTelegramId(), QUERY_LIST_STATE);
        assertEquals(String.format(CONFIG_DELETED.toString(), config.getConfigName()) + "\n" + USER_QUERIES, result);
    }

    @Test
    @DisplayName("Should return not found message when config does not exist")
    void testProcessDeleteConfirmCommand_ConfigNotFound() {
        String input = DELETE_CONFIRM + " 1"; // Example input
        long configId = 1L;

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.empty());

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(userStateCacheService).clearUserState(appUser.getTelegramId());
        assertEquals(CONFIG_NOT_FOUND.toString(), result);
    }

    @Test
    @DisplayName("Should process delete cancel command")
    void testProcessDeleteCancelCommand() {
        String input = Prefix.QUERY + " some input"; // Example input

        when(queryListStateHandler.process(appUser, input)).thenReturn("Query List");

        String result = queryDeleteStateHandler.process(appUser, input);

        verify(userStateCacheService).putUserState(appUser.getTelegramId(), QUERY_LIST_STATE);
        assertEquals("Query List", result);
    }
}