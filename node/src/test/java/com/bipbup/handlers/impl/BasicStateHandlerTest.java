package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_BROADCAST_MESSAGE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import com.bipbup.enums.Role;
import com.bipbup.handlers.impl.message.BasicStateHandler;
import com.bipbup.service.bot.NotifierService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.utils.CommandMessageConstants;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.BROADCAST;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.SEARCH;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.ENTER_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.INCORRECT_PASSWORD;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.NO_PERMISSION;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.SEARCHING_COMPLETED;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.HELP;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.NEWQUERY;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.START;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.NO_SAVED_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_PROMPT;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.WELCOME;
import java.util.Collections;
import lombok.SneakyThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;


class BasicStateHandlerTest {

    private final String adminPassword = "admin123";

    @Mock
    private UserStateCacheService userStateCacheService;
    @Mock
    private ConfigService configService;
    @Mock
    private NotifierService notifierService;
    @InjectMocks
    private BasicStateHandler basicStateHandler;

    private AppUser appUser;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var password = basicStateHandler.getClass().getDeclaredField("adminPassword");
        password.setAccessible(true);
        password.set(basicStateHandler, "admin123");
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
        appUser.setRole(Role.ADMIN);
    }

    @Test
    @DisplayName("Should process start command")
    void testProcessStartCommand() {
        String result = basicStateHandler.process(appUser, START.getCommand());
        assertEquals(String.format(WELCOME.getTemplate(), appUser.getFirstName()), result);
    }

    @Test
    @DisplayName("Should process help command")
    void testProcessHelpCommand() {
        String result = basicStateHandler.process(appUser, HELP.getCommand());
        assertEquals(CommandMessageConstants.MessageTemplate.HELP.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process new query command")
    void testProcessNewQueryCommand() {
        String result = basicStateHandler.process(appUser, NEWQUERY.getCommand());
        verify(userStateCacheService).putUserState(appUser.getTelegramId(), WAIT_CONFIG_NAME_STATE);
        assertEquals(QUERY_PROMPT.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process my queries command with no saved queries")
    void testProcessMyQueriesCommand_NoSavedQueries() {
        when(configService.getConfigByUser(appUser)).thenReturn(Collections.emptyList());

        String result = basicStateHandler.process(appUser, MYQUERIES.getCommand());
        verify(userStateCacheService).clearUserState(appUser.getTelegramId());
        assertEquals(NO_SAVED_QUERIES.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process my queries command with saved queries")
    void testProcessMyQueriesCommand_WithSavedQueries() {
        when(configService.getConfigByUser(appUser)).thenReturn(Collections.singletonList(new AppUserConfig()));

        String result = basicStateHandler.process(appUser, MYQUERIES.getCommand());
        verify(userStateCacheService).putUserState(appUser.getTelegramId(), QUERY_LIST_STATE);
        assertEquals(USER_QUERIES.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process broadcast command with correct password")
    void testProcessBroadcastCommand_CorrectPassword() {
        String input = BROADCAST.getCommand() + " " + adminPassword;
        String result = basicStateHandler.process(appUser, input);
        verify(userStateCacheService).putUserState(appUser.getTelegramId(), WAIT_BROADCAST_MESSAGE);
        assertEquals(ENTER_MESSAGE.getTemplate(), result);
    }

    @Test
    @DisplayName("Should deny broadcast command with incorrect password")
    void testProcessBroadcastCommand_IncorrectPassword() {
        String input = BROADCAST.getCommand() + " wrongPassword";
        String result = basicStateHandler.process(appUser, input);
        assertEquals(INCORRECT_PASSWORD.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process search command")
    void testProcessSearchCommand() {
        String input = SEARCH.getCommand() + " " + adminPassword;
        String result = basicStateHandler.process(appUser, input);
        verify(notifierService).searchNewVacancies();
        assertEquals(SEARCHING_COMPLETED.getTemplate(), result);
    }

    @Test
    @DisplayName("Should deny search command with incorrect password")
    void testProcessSearchCommand_IncorrectPassword() {
        String input = SEARCH.getCommand() + " wrongPassword";
        String result = basicStateHandler.process(appUser, input);
        assertEquals(INCORRECT_PASSWORD.getTemplate(), result);
    }

    @Test
    @DisplayName("Should deny admin commands for non-admin users")
    void testAdminCommand_NonAdminUser() {
        appUser.setRole(Role.USER); // Change role to non-admin
        String result = basicStateHandler.process(appUser, BROADCAST.getCommand() + " " + adminPassword);
        assertEquals(NO_PERMISSION.getTemplate(), result);
    }
}