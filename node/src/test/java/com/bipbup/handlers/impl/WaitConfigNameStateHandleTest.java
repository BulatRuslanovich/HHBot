package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.utils.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.handlers.impl.WaitConfigNameStateHandle.CANCEL_COMMAND;
import static com.bipbup.handlers.impl.WaitConfigNameStateHandle.CANCEL_MESSAGE;
import static com.bipbup.handlers.impl.WaitConfigNameStateHandle.CONFIG_EXISTS_MESSAGE_TEMPLATE;
import static com.bipbup.handlers.impl.WaitConfigNameStateHandle.ENTER_QUERY_MESSAGE_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class WaitConfigNameStateHandleTest {

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private WaitConfigNameStateHandle waitConfigNameStateHandle;

    private AppUser appUser;
    private List<AppUserConfig> configs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        appUser = new AppUser();
        appUser.setFirstName("TestUser");

        configs = new ArrayList<>();
        appUser.setAppUserConfigs(configs);
    }

    @Test
    void testProcessCancelCommand() {
        String result = waitConfigNameStateHandle.process(appUser, CANCEL_COMMAND);

        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        assertEquals(CANCEL_MESSAGE, result);
    }

    @Test
    void testProcessExistingConfig() {
        String existingConfigName = "ExistingConfig";
        AppUserConfig existingConfig = new AppUserConfig();
        existingConfig.setConfigName(existingConfigName);
        configs.add(existingConfig);

        String result = waitConfigNameStateHandle.process(appUser, existingConfigName);

        verify(userUtil).updateUserState(appUser, BASIC_STATE);
        assertEquals(String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, existingConfigName), result);
    }

    @Test
    void testProcessNewConfig() {
        String newConfigName = "NewConfig";

        String result = waitConfigNameStateHandle.process(appUser, newConfigName);

        verify(userUtil).updateUserState(appUser, WAIT_QUERY_STATE);
        assertEquals(String.format(ENTER_QUERY_MESSAGE_TEMPLATE, newConfigName), result);

        assertEquals(1, configs.size());
        assertEquals(newConfigName, configs.get(0).getConfigName());
        assertEquals(appUser, configs.get(0).getAppUser());
    }
}
