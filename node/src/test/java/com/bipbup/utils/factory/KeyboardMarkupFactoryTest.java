package com.bipbup.utils.factory;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.ConfigService;
import com.bipbup.utils.CommandMessageConstants;
import com.bipbup.utils.Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class KeyboardMarkupFactoryTest {
    @Mock
    private ConfigService configService;

    @Mock
    private Encoder encoder;

    @InjectMocks
    private KeyboardMarkupFactory keyboardMarkupFactory;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
    }

    @Test
    @DisplayName("Should create user config list keyboard")
    void testCreateUserConfigListKeyboard() {
        // Arrange
        List<AppUserConfig> configs = new ArrayList<>();
        AppUserConfig config1 = new AppUserConfig();
        config1.setUserConfigId(1L);
        config1.setConfigName("Config 1");

        AppUserConfig config2 = new AppUserConfig();
        config2.setUserConfigId(2L);
        config2.setConfigName("Config 2");

        configs.add(config1);
        configs.add(config2);

        when(configService.getByUser(appUser)).thenReturn(configs);
        when(encoder.hashOf(1L)).thenReturn("hash1");
        when(encoder.hashOf(2L)).thenReturn("hash2");

        // Act
        InlineKeyboardMarkup markup = keyboardMarkupFactory.createUserConfigListKeyboard(appUser);

        // Assert
        assertNotNull(markup);
        assertEquals(2, markup.getKeyboard().get(0).size());
        assertEquals(1, markup.getKeyboard().size());
        assertEquals("Config 1", markup.getKeyboard().get(0).get(0).getText());
        assertEquals("Config 2", markup.getKeyboard().get(0).get(1).getText());
    }

    @Test
    @DisplayName("Should create config management keyboard")
    void testCreateConfigManagementKeyboard() {
        // Arrange
        String callbackData = "someCallbackData";
        when(encoder.hashOf(anyLong())).thenReturn("hash");

        // Act
        InlineKeyboardMarkup markup = keyboardMarkupFactory.createConfigManagementKeyboard(callbackData);

        // Assert
        assertNotNull(markup);
        assertEquals(2, markup.getKeyboard().size());
        assertEquals(2, markup.getKeyboard().get(0).size());
        assertEquals(1, markup.getKeyboard().get(1).size());
        assertEquals(CommandMessageConstants.ButtonText.UPDATE, markup.getKeyboard().get(0).get(0).getText());
        assertEquals(CommandMessageConstants.ButtonText.DELETE, markup.getKeyboard().get(0).get(1).getText());
        assertEquals(CommandMessageConstants.ButtonText.BACK, markup.getKeyboard().get(1).get(0).getText());
    }

    @Test
    @DisplayName("Should create delete confirmation keyboard")
    void testCreateDeleteConfirmationKeyboard() {
        // Arrange
        String callbackData = "someCallbackData";
        when(encoder.hashOf(anyLong())).thenReturn("hash");

        // Act
        InlineKeyboardMarkup markup = keyboardMarkupFactory.createDeleteConfirmationKeyboard(callbackData);

        // Assert
        assertNotNull(markup);
        assertEquals(1, markup.getKeyboard().size()); // Should have 2 buttons
        assertEquals(CommandMessageConstants.ButtonText.DELETE_CONFIRM, markup.getKeyboard().get(0).get(0).getText());
        assertEquals(CommandMessageConstants.ButtonText.DELETE_CANCEL, markup.getKeyboard().get(0).get(1).getText());
    }

    @Test
    @DisplayName("Should create update config keyboard")
    void testCreateUpdateConfigKeyboard() {
        // Arrange
        String callbackData = "someCallbackData";
        when(encoder.hashOf(anyLong())).thenReturn("hash");

        // Act
        InlineKeyboardMarkup markup = keyboardMarkupFactory.createUpdateConfigKeyboard(callbackData);

        // Assert
        assertNotNull(markup);
        assertEquals(4, markup.getKeyboard().size()); // Should have 7 buttons
        assertEquals(CommandMessageConstants.ButtonText.UPDATE_CONFIG_NAME, markup.getKeyboard().get(0).get(0).getText());
        assertEquals(CommandMessageConstants.ButtonText.BACK, markup.getKeyboard().get(3).get(0).getText());
    }
}