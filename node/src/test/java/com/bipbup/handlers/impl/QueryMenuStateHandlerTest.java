package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class QueryMenuStateHandlerTest {

    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String MYQUERIES_COMMAND = "/myqueries";
    protected static final String NEWQUERY_COMMAND = "/newquery";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда была отменена.";

    @Mock
    private BasicStateHandler basicStateHandler;

    @Mock
    private AppUserDAO appUserDAO;

    @InjectMocks
    private QueryMenuStateHandler queryMenuStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setFirstName("TestUser");
    }

    @Test
    void testProcessCancelCommand() {
        String result = queryMenuStateHandler.process(appUser, CANCEL_COMMAND);

        assertEquals(MESSAGE_COMMAND_CANCELLED, result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testProcessMyQueriesCommand() {
        when(basicStateHandler.process(any(AppUser.class), eq(MYQUERIES_COMMAND)))
                .thenReturn("My Queries Output");

        String result = queryMenuStateHandler.process(appUser, MYQUERIES_COMMAND);

        assertEquals("My Queries Output", result);
        verify(basicStateHandler, times(1)).process(appUser, MYQUERIES_COMMAND);
    }

    @Test
    void testHandleNewQueryCommand() {
        when(basicStateHandler.process(any(AppUser.class), eq(NEWQUERY_COMMAND)))
                .thenReturn("New Query Output");

        String result = queryMenuStateHandler.process(appUser, NEWQUERY_COMMAND);

        assertEquals("New Query Output", result);
        verify(basicStateHandler, times(1)).process(appUser, NEWQUERY_COMMAND);
    }

    @Test
    void testProcessDeleteCommand() {
        String result = queryMenuStateHandler.process(appUser, QueryMenuStateHandler.DELETE_PREFIX + "123");

        assertEquals(QueryMenuStateHandler.MESSAGE_DELETE_CONFIRMATION, result);
        assertEquals(QUERY_DELETE_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleUnknownCommand() {
        String result = queryMenuStateHandler.process(appUser, "unknown_command");

        assertEquals("", result); // Возвращает пустую строку при неизвестной команде
        verifyNoInteractions(appUserDAO); // Нет взаимодействий с базой данных
    }
}
