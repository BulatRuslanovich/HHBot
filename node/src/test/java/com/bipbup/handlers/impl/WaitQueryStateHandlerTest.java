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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class WaitQueryStateHandlerTest {

    @Mock
    private AppUserDAO appUserDAO;

    @Mock
    private AppUserConfigDAO appUserConfigDAO;

    @InjectMocks
    private WaitQueryStateHandler waitQueryStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setFirstName("TestUser");
    }

    @Test
    void CancelCommand() {
        AppUserConfig lastConfig = AppUserConfig.builder().configName("TestConfig").build();
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of(lastConfig));

        String result = waitQueryStateHandler.process(appUser, WaitQueryStateHandler.CANCEL_COMMAND);

        assertEquals(WaitQueryStateHandler.CANCEL_MESSAGE, result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
        verify(appUserConfigDAO, times(1)).delete(lastConfig);
    }

    @Test
    void InvalidQuery() {
        AppUserConfig lastConfig = AppUserConfig.builder().configName("TestConfig").build();
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of(lastConfig));

        String result = waitQueryStateHandler.process(appUser, "");

        assertEquals(WaitQueryStateHandler.INVALID_QUERY_MESSAGE, result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
        verify(appUserConfigDAO, times(1)).delete(lastConfig);
    }

    @Test
    void ValidQuery() {
        String validQuery = "SELECT * FROM users WHERE age > 18";
        AppUserConfig lastConfig = AppUserConfig.builder().configName("TestConfig").build();
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of(lastConfig));

        String result = waitQueryStateHandler.process(appUser, validQuery);

        assertEquals(String.format(WaitQueryStateHandler.QUERY_SET_MESSAGE_TEMPLATE, validQuery, lastConfig.getConfigName()), result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
        verify(appUserConfigDAO, times(1)).saveAndFlush(lastConfig);
        assertEquals(validQuery, lastConfig.getQueryText());
    }

    @Test
    void NoConfigurationsFound() {
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                waitQueryStateHandler.process(appUser, "Some query")
        );

        assertEquals("No configurations found for user.", exception.getMessage());
        verify(appUserConfigDAO, times(1)).findByAppUser(appUser);
        verifyNoMoreInteractions(appUserConfigDAO);
        verifyNoInteractions(appUserDAO);
    }
}
