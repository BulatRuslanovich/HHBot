package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static com.bipbup.handlers.impl.BasicStateHandler.NO_SAVED_QUERIES_MESSAGE;
import static com.bipbup.handlers.impl.BasicStateHandler.QUERY_PROMPT_MESSAGE;
import static com.bipbup.handlers.impl.BasicStateHandler.USER_QUERIES_MESSAGE;
import static com.bipbup.handlers.impl.BasicStateHandler.WELCOME_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BasicStateHandlerTest {

    @MockBean
    private AppUserDAO appUserDAO;

    @MockBean
    private AppUserConfigDAO appUserConfigDAO;

    @Autowired
    private BasicStateHandler basicStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setState(AppUserState.BASIC_STATE);
        basicStateHandler.init();
    }

    @Test
    void testStartInteraction() {
        String result = basicStateHandler.process(appUser, "/start");
        assertEquals(String.format(WELCOME_MESSAGE, "John"), result);
    }

    @Test
    void testAddQueryOutput_ChangesState() {
        String result = basicStateHandler.process(appUser, "/newquery");
        assertEquals(QUERY_PROMPT_MESSAGE, result);
        assertEquals(AppUserState.WAIT_CONFIG_NAME_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testShowQueriesOutput_NoQueries() {
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(Collections.emptyList());

        String result = basicStateHandler.process(appUser, "/myqueries");

        assertEquals(NO_SAVED_QUERIES_MESSAGE, result);
    }

    @Test
    void testShowQueriesOutput_WithQueries() {
        AppUserConfig appUserConfig = mock(AppUserConfig.class);
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(Collections.singletonList(appUserConfig));

        String result = basicStateHandler.process(appUser, "/myqueries");

        assertEquals(USER_QUERIES_MESSAGE, result);
        assertEquals(AppUserState.QUERY_LIST_STATE, appUser.getState());
        verify(appUserDAO, times(1)).saveAndFlush(appUser);
    }

    @Test
    void testAddQueryOutput_AlreadyInWaitConfigNameState() {
        appUser.setState(AppUserState.WAIT_CONFIG_NAME_STATE);

        String result = basicStateHandler.process(appUser, "/newquery");

        assertEquals(QUERY_PROMPT_MESSAGE, result);
        assertEquals(AppUserState.WAIT_CONFIG_NAME_STATE, appUser.getState());
        verify(appUserDAO, never()).saveAndFlush(appUser);
    }

    @Test
    void testShowQueriesOutput_AlreadyInQueryListState() {
        appUser.setState(AppUserState.QUERY_LIST_STATE);
        AppUserConfig appUserConfig = mock(AppUserConfig.class);
        when(appUserConfigDAO.findByAppUser(appUser)).thenReturn(Collections.singletonList(appUserConfig));

        String result = basicStateHandler.process(appUser, "/myqueries");

        assertEquals(USER_QUERIES_MESSAGE, result);
        assertEquals(AppUserState.QUERY_LIST_STATE, appUser.getState());
        verify(appUserDAO, never()).saveAndFlush(appUser);
    }

    @Test
    void testProcess_EmptyCommand() {
        String result = basicStateHandler.process(appUser, "");
        assertEquals("", result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
    }

    @Test
    void testProcess_NullCommand() {
        String result = basicStateHandler.process(appUser, null);
        assertEquals("", result);
        assertEquals(AppUserState.BASIC_STATE, appUser.getState());
    }
}
