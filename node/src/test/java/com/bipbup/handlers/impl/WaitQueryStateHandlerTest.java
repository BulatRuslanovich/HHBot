package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.utils.UserConfigUtil;
import com.bipbup.utils.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.handlers.impl.WaitQueryStateHandler.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class WaitQueryStateHandlerTest {

    @Mock
    private UserConfigUtil userConfigUtil;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private WaitQueryStateHandler waitQueryStateHandler;

    private AppUser appUser;
    private AppUserConfig lastConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        appUser = new AppUser();
        appUser.setFirstName("TestUser");

        lastConfig = new AppUserConfig();
        lastConfig.setConfigName("LastConfig");
        lastConfig.setAppUser(appUser);

        List<AppUserConfig> configs = new ArrayList<>();
        configs.add(lastConfig);
        appUser.setAppUserConfigs(configs);
    }

    @Test
    void testProcessCancelCommand() {
        String result = waitQueryStateHandler.process(appUser, CANCEL_COMMAND);

        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        verify(userConfigUtil).removeConfig(lastConfig);
        assertEquals(COMMAND_CANCELLED_MESSAGE, result);
    }

    @Test
    void testProcessInvalidQuery() {
        String result = waitQueryStateHandler.process(appUser, "");

        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        verify(userConfigUtil).removeConfig(lastConfig);
        assertEquals(INVALID_QUERY_MESSAGE, result);
    }

    @Test
    void testProcessValidQuery() {
        String validQuery = "valid query";

        String result = waitQueryStateHandler.process(appUser, validQuery);

        verify(userConfigUtil).updateConfigQuery(lastConfig, validQuery.replace("+", "%2B"));
        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        assertEquals(String.format(QUERY_SET_MESSAGE_TEMPLATE, validQuery, lastConfig.getConfigName()), result);
    }

    @Test
    void testProcessValidQueryWithPlusSign() {
        String validQuery = "valid+query";

        String result = waitQueryStateHandler.process(appUser, validQuery);

        verify(userConfigUtil).updateConfigQuery(lastConfig, "valid%2Bquery");
        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        assertEquals(String.format(QUERY_SET_MESSAGE_TEMPLATE, validQuery, lastConfig.getConfigName()), result);
    }
}
