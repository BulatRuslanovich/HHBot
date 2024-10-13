package com.bipbup.utils;

import org.hashids.Hashids;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

class DecoderTest {
    @Mock
    private Hashids hashids;

    @InjectMocks
    private Decoder decoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should parse valid ID from callback string")
    void testParseIdFromCallback_ValidId() {
        // Arrange
        String callback = "someprefix_12345";
        String hash = "12345";
        long expectedId = 67890L;

        // Mocking Hashids decode method
        when(hashids.decode(hash)).thenReturn(new long[]{expectedId});

        // Act
        Long result = decoder.parseIdFromCallback(callback);

        // Assert
        assertEquals(expectedId, result);
    }

    @Test
    @DisplayName("Should return 0 for invalid hash in callback")
    void testParseIdFromCallback_InvalidHash() {
        // Arrange
        String callback = "someprefix_invalid";
        String hash = "invalid";

        // Mocking Hashids decode method
        when(hashids.decode(hash)).thenReturn(new long[0]);

        // Act
        Long result = decoder.parseIdFromCallback(callback);

        // Assert
        assertNull( result);
    }

    @Test
    @DisplayName("Should return 0 for empty callback string")
    void testParseIdFromCallback_EmptyCallback() {
        // Arrange
        String callback = "";

        // Act
        Long result = decoder.parseIdFromCallback(callback);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return 0 for callback with no underscore")
    void testParseIdFromCallback_NoUnderscore() {
        // Arrange
        String callback = "someprefix12345";

        // Act
        Long result = decoder.parseIdFromCallback(callback);

        // Assert
        assertNull(result);
    }
}