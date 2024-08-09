package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class QueryDeleteStateHandlerTest {

    @Mock
    private AppUserDAO appUserDAO;

    @Mock
    private AppUserConfigDAO appUserConfigDAO;

    @InjectMocks
    private QueryDeleteStateHandler queryDeleteStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setState(AppUserState.QUERY_DELETE_STATE);
    }

    @Test
    void testCancelCommand() {
        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.COMMAND_CANCEL);

        assertEquals(QueryDeleteStateHandler.MESSAGE_COMMAND_CANCELLED, result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleDeleteYesCommand_ConfigExists() {
        AppUserConfig config = new AppUserConfig();
        config.setUserConfigId(1L);
        when(appUserConfigDAO.findById(1L)).thenReturn(Optional.of(config));

        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.PREFIX_DELETE_YES + "1");

        assertEquals(QueryDeleteStateHandler.MESSAGE_CONFIGURATION_DELETED, result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
        verify(appUserConfigDAO, times(1)).delete(config);
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleDeleteYesCommand_ConfigNotExists() {
        when(appUserConfigDAO.findById(1L)).thenReturn(Optional.empty());

        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.PREFIX_DELETE_YES + "1");

        assertEquals(QueryDeleteStateHandler.MESSAGE_CONFIGURATION_NOT_FOUND, result);
        assertEquals(AppUserState.QUERY_DELETE_STATE, appUser.getState());
        verify(appUserConfigDAO, never()).delete(any());
        verify(appUserDAO, never()).saveAndFlush(appUser);
    }

    @Test
    void testHandleDeleteNoCommand() {
        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.COMMAND_DELETE_NO);

        assertEquals(QueryDeleteStateHandler.MESSAGE_CONFIGURATION_NOT_DELETED, result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testProcess_InvalidConfigId() {
        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.PREFIX_DELETE_YES + "invalid");

        assertEquals(QueryDeleteStateHandler.MESSAGE_ERROR_PROCESSING_COMMAND, result);
        assertEquals(AppUserState.QUERY_DELETE_STATE, appUser.getState());
        verify(appUserConfigDAO, never()).delete(any());
        verify(appUserDAO, never()).saveAndFlush(appUser);
    }

    @Test
    void testProcess_UnexpectedError() {
        doThrow(new RuntimeException("Unexpected error")).when(appUserDAO).saveAndFlush(any());

        String result = queryDeleteStateHandler.process(appUser, QueryDeleteStateHandler.COMMAND_CANCEL);

        assertEquals(QueryDeleteStateHandler.MESSAGE_UNEXPECTED_ERROR, result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
    }
}
