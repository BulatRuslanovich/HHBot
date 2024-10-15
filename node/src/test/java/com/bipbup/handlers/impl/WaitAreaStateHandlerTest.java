package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.impl.message.WaitAreaStateHandler;
import com.bipbup.service.net.AreaService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.ANY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ANY_AREA_SET;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.AREA_SET;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.COMMAND_CANCELLED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.INVALID_INPUT;
import com.bipbup.utils.HandlerUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class WaitAreaStateHandlerTest {

    @Mock
    private ConfigService configService;

    @Mock
    private HandlerUtils handlerUtils;

    @Mock
    private UserStateCacheService userStateCacheService;

    @Mock
    private AreaService areaService;

    @InjectMocks
    private WaitAreaStateHandler waitAreaStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    @Test
    @DisplayName("Should process valid area name and save it")
    void testProcessValidAreaName() {
        String input = "New Area";
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(handlerUtils.isCancelCommand(input)).thenReturn(false);
        when(handlerUtils.isBasicCommand(input)).thenReturn(false);
        when(handlerUtils.fetchConfig(appUser)).thenReturn(Optional.of(config));

        when(areaService.getAreaIdByName("New Area")).thenReturn(1);

        String result = waitAreaStateHandler.process(appUser, input);

        verify(configService).saveConfig(config);
        verify(userStateCacheService).clearUserState(appUser.getTelegramId());
        assertEquals(String.format(AREA_SET.getTemplate(), "New Area", config.getConfigName()), result);
    }

    @Test
    @DisplayName("Should process ANY input and clear area")
    void testProcessAnyInput() {
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(handlerUtils.isCancelCommand(anyString())).thenReturn(false);
        when(handlerUtils.isBasicCommand(anyString())).thenReturn(false);
        when(handlerUtils.fetchConfig(appUser)).thenReturn(Optional.of(config));

        String result = waitAreaStateHandler.process(appUser, ANY);

        verify(configService).saveConfig(config);
        verify(userStateCacheService).clearUserState(appUser.getTelegramId());
        assertEquals(String.format(ANY_AREA_SET.getTemplate(), config.getConfigName()), result);
    }


    @Test
    @DisplayName("Should process cancel command")
    void testProcessCancelCommand() {
        String input = "/cancel";
        when(handlerUtils.isCancelCommand("/cancel")).thenReturn(true);
        when(handlerUtils.processCancelCommand(appUser)).thenReturn(COMMAND_CANCELLED.getTemplate());

        String result = waitAreaStateHandler.process(appUser, input);

        assertEquals(COMMAND_CANCELLED.getTemplate(), result);
    }


    @Test
    @DisplayName("Should process invalid area name")
    void testProcessInvalidAreaName() {
        String input = "Invalid Area";

        when(areaService.getAreaIdByName(input)).thenReturn(null);
        when(handlerUtils.isCancelCommand(input)).thenReturn(false);
        when(handlerUtils.isBasicCommand(input)).thenReturn(false);
        when(handlerUtils.processInvalidInput(appUser)).thenReturn(INVALID_INPUT.getTemplate());

        String result = waitAreaStateHandler.process(appUser, input);

        assertEquals(INVALID_INPUT.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process valid area name without config")
    void testProcessValidAreaNameWithoutConfig() {
        String input = "Kazan";

        when(handlerUtils.isCancelCommand(input)).thenReturn(false);
        when(handlerUtils.isBasicCommand(input)).thenReturn(false);
        when(areaService.getAreaIdByName(input)).thenReturn(34);
        when(handlerUtils.fetchConfig(appUser)).thenReturn(Optional.empty());
        when(handlerUtils.processConfigNotFoundMessage(appUser)).thenReturn(CONFIG_NOT_FOUND.getTemplate());

        String result = waitAreaStateHandler.process(appUser, input);

        assertEquals(CONFIG_NOT_FOUND.getTemplate(), result);
    }
}
