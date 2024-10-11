package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.AreaService;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.bipbup.utils.CommandMessageConstants.ANY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WaitAreaStateHandlerTest {

    @Mock
    private UserService userService;

    @Mock
    private ConfigService configService;

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

        when(configService.getById(anyLong())).thenReturn(Optional.of(config));
        when(areaService.getAreaIdByName("New Area")).thenReturn(1);

        String result = waitAreaStateHandler.process(appUser, input);

        verify(configService).save(config);
        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(String.format(AREA_SET.getTemplate(), "New Area", config.getConfigName()), result);
    }

    @Test
    @DisplayName("Should process ANY input and clear area")
    void testProcessAnyInput() {
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(configService.getById(anyLong())).thenReturn(Optional.of(config));

        String result = waitAreaStateHandler.process(appUser, ANY);

        verify(configService).save(config);
        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(String.format(ANY_AREA_SET.getTemplate(), config.getConfigName()), result);
    }


    @Test
    @DisplayName("Should process cancel command")
    void testProcessCancelCommand() {
        String input = "/cancel";

        String result = waitAreaStateHandler.process(appUser, input);

        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(COMMAND_CANCELLED.getTemplate(), result);
    }


    @Test
    @DisplayName("Should process invalid area name")
    void testProcessInvalidAreaName() {
        String input = "Invalid Area";

        when(areaService.getAreaIdByName(input)).thenReturn(null);

        String result = waitAreaStateHandler.process(appUser, input);

        assertEquals(INVALID_INPUT.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process valid area name without config")
    void testProcessValidAreaNameWithoutConfig() {
        String input = "Kazan";

        when(areaService.getAreaIdByName(input)).thenReturn(34);

        String result = waitAreaStateHandler.process(appUser, input);

        assertEquals(CONFIG_NOT_FOUND.getTemplate(), result);
    }
}
