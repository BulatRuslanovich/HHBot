package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WaitConfigNameStateHandlerTest {

    @Mock
    private AppUserDAO appUserDAO;

    @Mock
    private AppUserConfigDAO appUserConfigDAO;

    @InjectMocks
    private WaitConfigNameStateHandler waitConfigNameStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setFirstName("TestUser");
    }

    @Test
    void existingConfig() {
        String configName = "ExistingConfig";
        AppUserConfig existingConfig = AppUserConfig.builder().configName(configName).build();
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of(existingConfig));

        String result = waitConfigNameStateHandler.process(appUser, configName);

        assertEquals(String.format(WaitConfigNameStateHandler.CONFIG_EXISTS_MESSAGE_TEMPLATE, configName), result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
        verify(appUserConfigDAO, times(1)).findByAppUser(appUser); // Expecting one call to findByAppUser
    }

    @Test
    void newConfig_WithExistingConfig() {
        String configName = "NewConfig";
        AppUserConfig existingConfig = AppUserConfig.builder().configName(configName).build();
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of(existingConfig));

        String result = waitConfigNameStateHandler.process(appUser, configName);

        assertEquals(String.format(WaitConfigNameStateHandler.CONFIG_EXISTS_MESSAGE_TEMPLATE, configName), result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
        verify(appUserConfigDAO, times(1)).findByAppUser(appUser);
    }
}
