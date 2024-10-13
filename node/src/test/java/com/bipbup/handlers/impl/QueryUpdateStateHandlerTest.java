package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import com.bipbup.handlers.impl.callback.QueryListStateHandler;
import com.bipbup.handlers.impl.callback.QueryUpdateStateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.ConfigCacheService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
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

class QueryUpdateStateHandlerTest {

    @Mock
    private UserStateCacheService userStateCacheService;

    @Mock
    private ConfigCacheService configCacheService;

    @Mock
    private ConfigService configService;

    @Mock
    private Decoder decoder;

    @Mock
    private QueryListStateHandler queryListStateHandler;

    @InjectMocks
    private QueryUpdateStateHandler queryUpdateStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    @Test
    @DisplayName("Should process back to query menu command")
    void testProcessBackToQueryMenuCommand() {
        String input = Prefix.QUERY + " some command";

        when(queryListStateHandler.process(appUser, input)).thenReturn("Query Menu");

        String result = queryUpdateStateHandler.process(appUser, input);

        verify(queryListStateHandler).process(appUser, input);
        assertEquals("Query Menu", result);
    }

    @Test
    @DisplayName("Should process update config name command")
    void testProcessUpdateConfigNameCommand() {
        String input = Prefix.UPDATE_CONFIG_NAME + " 1";
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.of(config));

        String result = queryUpdateStateHandler.process(appUser, input);

        verify(userStateCacheService).putUserState(appUser.getTelegramId(), WAIT_CONFIG_NAME_STATE);
        verify(configCacheService).putConfigId(appUser.getTelegramId(), configId);
        assertEquals(String.format(ENTER_CONFIG_NAME.getTemplate(), config.getConfigName()), result);
    }

    @Test
    @DisplayName("Should return config not found message")
    void testProcessUpdateConfigCommandNotFound() {
        String input = Prefix.UPDATE_CONFIG_NAME + " 1";
        long configId = 1L;

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.empty());

        String result = queryUpdateStateHandler.process(appUser, input);

        verify(userStateCacheService).clearUserState(appUser.getTelegramId());
        assertEquals(CONFIG_NOT_FOUND.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process update query command")
    void testProcessUpdateQueryCommand() {
        String input = Prefix.UPDATE_QUERY + " 2";
        long configId = 2L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Query Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.of(config));

        String result = queryUpdateStateHandler.process(appUser, input);

        verify(userStateCacheService).putUserState(appUser.getTelegramId(), WAIT_QUERY_STATE);
        verify(configCacheService).putConfigId(appUser.getTelegramId(), configId);
        assertEquals(String.format(ENTER_QUERY.getTemplate(), config.getConfigName()), result);
    }
}
