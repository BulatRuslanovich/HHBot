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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class QueryListStateHandlerTest {

    @Mock
    private AppUserDAO appUserDAO;

    @Mock
    private AppUserConfigDAO appUserConfigDAO;

    @Mock
    private BasicStateHandler basicStateHandler;

    @InjectMocks
    private QueryListStateHandler queryListStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setState(AppUserState.QUERY_LIST_STATE);
    }

    @Test
    void testCancelCommand() {
        String result = queryListStateHandler.process(appUser, QueryListStateHandler.COMMAND_CANCEL);

        assertEquals(QueryListStateHandler.MESSAGE_COMMAND_CANCELLED, result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleQueryCommand_ConfigExists() {
        AppUserConfig config = new AppUserConfig();
        config.setUserConfigId(1L);
        config.setConfigName("Test Config");
        config.setQueryText("Test Query");

        when(appUserConfigDAO.findById(1L)).thenReturn(Optional.of(config));

        String result = queryListStateHandler.process(appUser, QueryListStateHandler.PREFIX_QUERY + "1");

        assertEquals(String.format(QueryListStateHandler.QUERY_OUTPUT_FORMAT, config.getConfigName(), config.getQueryText()), result);
        assertEquals(AppUserState.QUERY_MENU_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleQueryCommand_ConfigNotExists() {
        when(appUserConfigDAO.findById(1L)).thenReturn(Optional.empty());

        String result = queryListStateHandler.process(appUser, QueryListStateHandler.PREFIX_QUERY + "1");

        assertEquals(QueryListStateHandler.MESSAGE_CONFIGURATION_NOT_FOUND, result);
        assertEquals(AppUserState.QUERY_LIST_STATE, appUser.getState());
        verify(appUserDAO, times(0)).saveAndFlush(appUser);
    }

    @Test
    void testHandleQueryCommand_InvalidQueryId() {
        String result = queryListStateHandler.process(appUser, QueryListStateHandler.PREFIX_QUERY + "invalid");

        assertEquals("", result);
        assertEquals(AppUserState.QUERY_LIST_STATE, appUser.getState());
        verify(appUserDAO, never()).saveAndFlush(any());
    }

    @Test
    void testBasicStateHandlerInvocation() {
        when(basicStateHandler.process(any(AppUser.class), eq(QueryListStateHandler.COMMAND_MY_QUERIES)))
                .thenReturn("Mocked response");

        String result = queryListStateHandler.process(appUser, QueryListStateHandler.COMMAND_MY_QUERIES);

        assertEquals("Mocked response", result);
        verify(basicStateHandler, times(1)).process(appUser, QueryListStateHandler.COMMAND_MY_QUERIES);
    }

    @Test
    void testProcess_UnexpectedError() {
        String result = queryListStateHandler.process(appUser, QueryListStateHandler.PREFIX_QUERY + "1");

        assertEquals(QueryListStateHandler.MESSAGE_CONFIGURATION_NOT_FOUND, result);
        assertEquals(AppUserState.QUERY_LIST_STATE, appUser.getState());
        verify(appUserDAO, times(0)).saveAndFlush(appUser);
    }
}
