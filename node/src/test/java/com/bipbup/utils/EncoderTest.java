package com.bipbup.utils;

import org.hashids.Hashids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class EncoderTest {
    @Mock
    private Hashids hashids;

    @InjectMocks
    private Encoder encoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return encoded string for valid number")
    void testHashOf_ValidNumber() {
        // Arrange
        long number = 12345L;
        String expectedHash = "abcde";

        // Mocking Hashids encode method
        when(hashids.encode(number)).thenReturn(expectedHash);

        // Act
        String result = encoder.hashOf(number);

        // Assert
        assertEquals(expectedHash, result);
    }

    @Test
    @DisplayName("Should return empty string for null or invalid encoding")
    void testHashOf_InvalidEncoding() {
        // Arrange
        long number = 67890L;

        when(hashids.encode(number)).thenReturn(null);

        // Act
        String result = encoder.hashOf(number);

        // Assert
        assertEquals("", result);
    }
}