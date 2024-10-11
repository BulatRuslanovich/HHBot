package com.bipbup.handlers;

import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static com.bipbup.utils.CommandMessageConstants.BotCommand.CANCEL;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancellableStateHandlerTest {
    @Mock
    private UserService userService;

    @Mock
    private ConfigService configService;

    @Mock
    private BasicStateHandler basicStateHandler;

    @InjectMocks
    private CancellableStateHandlerImpl cancellableStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setTelegramId(12345L);
        appUser.setFirstName("TestUser");
    }

    // Concrete implementation for testing
    private static class CancellableStateHandlerImpl extends CancellableStateHandler {
        public CancellableStateHandlerImpl(UserService userService, ConfigService configService, BasicStateHandler basicStateHandler) {
            super(userService, configService, basicStateHandler);
        }

        @Override
        public String process(AppUser user, String input) {
            return null; // Not needed for this test
        }
    }

    @Test
    @DisplayName("Should identify cancel command")
    void testIsCancelCommand() {
        assertTrue(cancellableStateHandler.isCancelCommand(CANCEL.getCommand()));
        assertFalse(cancellableStateHandler.isCancelCommand("some_other_command"));
    }

    @Test
    @DisplayName("Should identify basic commands")
    void testIsBasicCommand() {
        assertTrue(cancellableStateHandler.isBasicCommand("/myqueries"));
        assertTrue(cancellableStateHandler.isBasicCommand("/newquery"));
        assertTrue(cancellableStateHandler.isBasicCommand("/start"));
        assertTrue(cancellableStateHandler.isBasicCommand("/help"));
        assertFalse(cancellableStateHandler.isBasicCommand("/invalid_command"));
    }

    @Test
    @DisplayName("Should process cancel command")
    void testProcessCancelCommand() {
        String result = cancellableStateHandler.processCancelCommand(appUser);
        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(COMMAND_CANCELLED.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process basic command")
    void testProcessBasicCommand() {
        String input = "myqueries";
        when(basicStateHandler.process(appUser, input)).thenReturn("Processed Basic Command");

        String result = cancellableStateHandler.processBasicCommand(appUser, input);

        assertEquals("Processed Basic Command", result);
        verify(basicStateHandler).process(appUser, input);
    }

    @Test
    @DisplayName("Should process invalid input")
    void testProcessInvalidInput() {
        String result = cancellableStateHandler.processInvalidInput(appUser);
        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(INVALID_INPUT.getTemplate(), result);
    }

    @Test
    @DisplayName("Should process config not found message")
    void testProcessConfigNotFoundMessage() {
        when(configService.getSelectedConfigId(appUser.getTelegramId())).thenReturn(1L);
        cancellableStateHandler.processConfigNotFoundMessage(appUser);
        verify(configService).clearConfigSelection(appUser.getTelegramId());
        verify(userService).clearUserState(appUser.getTelegramId());
        assertEquals(CONFIG_NOT_FOUND.getTemplate(), cancellableStateHandler.processConfigNotFoundMessage(appUser));
    }

    @Test
    @DisplayName("Should fetch config when updating")
    void testFetchConfig_WhenUpdating() {
        when(configService.getSelectedConfigId(appUser.getTelegramId())).thenReturn(1L);
        when(configService.getById(1L)).thenReturn(Optional.of(new AppUserConfig()));

        Optional<AppUserConfig> config = cancellableStateHandler.fetchConfig(appUser);

        assertTrue(config.isPresent());
        verify(configService).clearConfigSelection(appUser.getTelegramId());
    }

    @Test
    @DisplayName("Should fetch last config when not updating")
    void testFetchLastConfig_WhenNotUpdating() {
        when(configService.getSelectedConfigId(appUser.getTelegramId())).thenReturn(null);
        when(configService.getByUser(appUser)).thenReturn(Collections.singletonList(new AppUserConfig()));

        Optional<AppUserConfig> config = cancellableStateHandler.fetchConfig(appUser);

        assertTrue(config.isPresent());
        verify(configService).getByUser(appUser);
    }

    @Test
    @DisplayName("Should return empty when no configs found")
    void testFetchLastConfig_NoConfigsFound() {
        when(configService.getSelectedConfigId(appUser.getTelegramId())).thenReturn(null);
        when(configService.getByUser(appUser)).thenReturn(Collections.emptyList());

        Optional<AppUserConfig> config = cancellableStateHandler.fetchConfig(appUser);

        assertFalse(config.isPresent());
        verify(configService).getByUser(appUser);
    }
}