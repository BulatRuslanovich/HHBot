package com.bipbup.handlers.impl;


import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import com.bipbup.service.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.DELETE_CONFIRMATION;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import com.bipbup.utils.Decoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class QueryMenuStateHandlerTest {

    @Mock
    private UserStateCacheService userStateCacheService;

    @Mock
    private ConfigService configService;

    @Mock
    private Decoder decoder;

    @Mock
    private BasicStateHandler basicStateHandler;

    @InjectMocks
    private QueryMenuStateHandler queryMenuStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    @Test
    @DisplayName("Should process back to query list command")
    void testProcessBackToQueryListCommand() {
        String input = MYQUERIES.getCommand();

        when(basicStateHandler.process(appUser, MYQUERIES.getCommand())).thenReturn("Query List");

        String result = queryMenuStateHandler.process(appUser, input);

        verify(basicStateHandler).process(appUser, MYQUERIES.getCommand());
        assertEquals("Query List", result);
    }

    @Test
    @DisplayName("Should process delete command and return confirmation")
    void testProcessDeleteCommand() {
        String input = Prefix.DELETE + " 1";
        long configId = 1L;
        AppUserConfig config = new AppUserConfig();
        config.setConfigName("Test Config");

        when(decoder.parseIdFromCallback(input)).thenReturn(configId);
        when(configService.getConfigById(configId)).thenReturn(Optional.of(config));

        String result = queryMenuStateHandler.process(appUser, input);

        verify(userStateCacheService).putUserState(appUser.getTelegramId(), AppUserState.QUERY_DELETE_STATE);
        assertEquals(String.format(DELETE_CONFIRMATION.getTemplate(), config.getConfigName()), result);
    }

    @Test
    @DisplayName("Should return empty string for non-command input")
    void testProcessNonCommandInput() {
        String input = "some other command";

        String result = queryMenuStateHandler.process(appUser, input);

        assertEquals("", result);
    }
}
