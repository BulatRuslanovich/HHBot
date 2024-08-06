package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.utils.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static com.bipbup.enums.AppUserState.*;
import static com.bipbup.handlers.impl.BasicStateHandler.NO_SAVED_QUERIES_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class BasicStateHandlerTest {

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private BasicStateHandler basicStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        appUser = new AppUser();
        appUser.setFirstName("TestUser");
    }

    @Test
    void testProcessStartCommand() {
        String result = basicStateHandler.process(appUser, "/start");

        assertEquals(String.format(BasicStateHandler.WELCOME_MESSAGE, "TestUser"), result);
    }

    @Test
    void testProcessNewQueryCommand() {
        String result = basicStateHandler.process(appUser, "/newquery");

        verify(userUtil).updateUserState(appUser, WAIT_CONFIG_NAME_STATE);
        assertEquals(BasicStateHandler.QUERY_PROMPT_MESSAGE, result);
    }

    @Test
    void testProcessMyQueriesCommandWithNoSavedQueries() {
        appUser.setAppUserConfigs(Collections.emptyList());

        String result = basicStateHandler.process(appUser, "/myqueries");

        assertEquals(NO_SAVED_QUERIES_MESSAGE, result);
    }

    @Test
    void testProcessMyQueriesCommandWithSavedQueries() {
        appUser.setAppUserConfigs(Collections.singletonList(new AppUserConfig()));

        String result = basicStateHandler.process(appUser, "/myqueries");

        verify(userUtil).updateUserState(appUser, QUERY_LIST_STATE);
        assertEquals(BasicStateHandler.USER_QUERIES_MESSAGE, result);
    }

    @Test
    void testProcessUnknownCommand() {
        String result = basicStateHandler.process(appUser, "/unknown");

        assertEquals("", result);
    }
}
