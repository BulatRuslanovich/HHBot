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
import static org.mockito.Mockito.*;

class QueryMenuStateHandlerTest {

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
    void testHandleCancelCommand() {
        String result = queryMenuStateHandler.process(appUser, QueryMenuStateHandler.COMMAND_CANCEL);

        assertEquals(QueryMenuStateHandler.MESSAGE_COMMAND_CANCELLED, result);
        assertEquals(BASIC_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testHandleMyQueriesCommand() {
        when(basicStateHandler.process(any(AppUser.class), eq(QueryMenuStateHandler.COMMAND_MY_QUERIES)))
                .thenReturn("My Queries Output");

        String result = queryMenuStateHandler.process(appUser, QueryMenuStateHandler.COMMAND_MY_QUERIES);

        assertEquals("My Queries Output", result);
        verify(basicStateHandler, times(1)).process(appUser, QueryMenuStateHandler.COMMAND_MY_QUERIES);
    }

    @Test
    void testHandleNewQueryCommand() {
        when(basicStateHandler.process(any(AppUser.class), eq(QueryMenuStateHandler.COMMAND_NEW_QUERY)))
                .thenReturn("New Query Output");

        String result = queryMenuStateHandler.process(appUser, QueryMenuStateHandler.COMMAND_NEW_QUERY);

        assertEquals("New Query Output", result);
        verify(basicStateHandler, times(1)).process(appUser, QueryMenuStateHandler.COMMAND_NEW_QUERY);
    }

    @Test
    void testHandleDeleteCommand() {
        String result = queryMenuStateHandler.process(appUser, QueryMenuStateHandler.PREFIX_DELETE + "123");

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
