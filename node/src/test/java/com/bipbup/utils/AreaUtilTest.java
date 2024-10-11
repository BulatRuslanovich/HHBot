package com.bipbup.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AreaUtilTest {

    private AreaUtil areaUtil;

    @Test
    @DisplayName("Should return area ID for valid area name")
    void testGetAreaIdFromApi_ValidAreaName() throws Exception {
        // Arrange
        String areaName = "Moscow";
        String jsonResponse = "[{\"id\":\"1\", \"name\":\"Moscow\"}]";

        HttpClient client = mock(HttpClient.class);
        areaUtil = new AreaUtil(client);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonResponse);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);

        // Act
        Integer areaId = areaUtil.getAreaIdFromApi(areaName);

        // Assert
        assertEquals(1, areaId);
    }

    @Test
    @DisplayName("Should return null for invalid area name")
    void testGetAreaIdFromApi_InvalidAreaName() throws Exception {
        // Arrange
        String areaName = "Unknown Area";
        String jsonResponse = "[{\"id\":\"1\", \"name\":\"Moscow\"}]";

        HttpClient client = mock(HttpClient.class);
        areaUtil = new AreaUtil(client);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonResponse);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);

        // Act
        Integer areaId = areaUtil.getAreaIdFromApi(areaName);

        // Assert
        assertNull(areaId);
    }

    @Test
    @DisplayName("Should return null for API error response")
    void testGetAreaIdFromApi_ApiError() throws Exception {
        // Arrange
        String areaName = "Moscow";

        HttpClient client = mock(HttpClient.class);
        areaUtil = new AreaUtil(client);
        var response = mock(HttpResponse.class);
        var request = mock(HttpRequest.class);

        when(response.statusCode()).thenReturn(500);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);
        when(response.request()).thenReturn(request);
        when(request.method()).thenReturn("POST");
        when(request.uri()).thenReturn(new URI("lol.com"));

        // Act
        Integer areaId = areaUtil.getAreaIdFromApi(areaName);

        // Assert
        assertNull(areaId);
    }

    @Test
    @DisplayName("Should return null for null area name")
    void testGetAreaIdFromApi_NullAreaName() {
        // Arrange
        HttpClient client = mock(HttpClient.class);
        areaUtil = new AreaUtil(client);

        // Act
        Integer areaId = areaUtil.getAreaIdFromApi(null);

        // Assert
        assertNull(areaId);
    }

    @Test
    @DisplayName("Should return null for IOException during API call")
    void testGetAreaIdFromApi_IOException() throws Exception {
        // Arrange
        String areaName = "Moscow";
        HttpClient client = mock(HttpClient.class);
        areaUtil = new AreaUtil(client);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenThrow(new IOException("Network error"));

        // Act
        Integer areaId = areaUtil.getAreaIdFromApi(areaName);

        // Assert
        assertNull(areaId);
    }
}