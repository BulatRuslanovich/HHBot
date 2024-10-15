package com.bipbup.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AreaUtilTest {

	ObjectMapper realObjectMapper = new ObjectMapper();
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private HttpClient client;
	@InjectMocks
	private AreaUtil areaUtil;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private HttpResponse<String> mockHttpResponse(int statusCode, String body)
			throws IOException, InterruptedException {
		HttpResponse<String> response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(statusCode);
		when(response.body()).thenReturn(body);
		when(client.send(any(HttpRequest.class), eq(ofString()))).thenReturn(response);
		return response;
	}

	@Test
	@DisplayName("Should return area ID for valid area name")
	void testGetAreaIdFromApi_ValidAreaName() throws Exception {
		// Arrange
		String areaName = "Moscow";
		String jsonResponse = "[{\"id\":\"1\", \"name\":\"Moscow\"}]";
		mockHttpResponse(200, jsonResponse);

		JsonNode jsonNode = realObjectMapper.readTree(jsonResponse);
		when(objectMapper.readTree(jsonResponse)).thenReturn(jsonNode);

		// Act
		Integer areaId = areaUtil.getAreaIdFromApi(areaName);

		// Assert
		assertEquals(1, areaId);
	}

	@Test
	@DisplayName("Should return null for invalid area name")
	void testGetAreaIdFromApi_InvalidAreaName() throws Exception {
		// Arrange
		String areaName = "Unknown";
		String jsonResponse = "[{\"id\":\"1\", \"name\":\"Moscow\"}]";
		var response = mockHttpResponse(200, jsonResponse);

		JsonNode jsonNode = realObjectMapper.readTree(jsonResponse);
		when(objectMapper.readTree(response.body())).thenReturn(jsonNode);

		// Act
		Integer areaId = areaUtil.getAreaIdFromApi(areaName);

		// Assert
		assertNull(areaId);
	}

	@Test
	@DisplayName("Should return null for empty area list")
	void testGetAreaIdFromApi_EmptyAreaList() throws Exception {
		// Arrange
		String areaName = "Moscow";
		String jsonResponse = "[]";  // Пустой JSON массив
		mockHttpResponse(200, jsonResponse);

		JsonNode jsonNode = realObjectMapper.readTree(jsonResponse);
		when(objectMapper.readTree(jsonResponse)).thenReturn(jsonNode);

		// Act
		Integer areaId = areaUtil.getAreaIdFromApi(areaName);

		// Assert
		assertNull(areaId);
	}

	@SneakyThrows
	@Test
	@DisplayName("Should return null for API error response")
	void testGetAreaIdFromApi_ApiError() {
		// Arrange
		String areaName = "Moscow";
		mockHttpResponse(500, "Internal Server Error");

		// Act
		Integer areaId = areaUtil.getAreaIdFromApi(areaName);

		// Assert
		assertNull(areaId);
	}

	@Test
	@DisplayName("Should return null for null area name")
	void testGetAreaIdFromApi_NullAreaName() {
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
		when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(new IOException("Network error"));

		// Act
		assertThrows(IOException.class, () -> areaUtil.getAreaIdFromApi(areaName));
	}

	@Test
	@DisplayName("Should return area ID for nested area structure")
	void testGetAreaIdFromApi_NestedAreas() throws Exception {
		// Arrange
		String areaName = "SubMoscow";
		String jsonResponse =
				"[{\"id\":\"1\", \"name\":\"Moscow\", \"areas\":[{\"id\":\"2\", \"name\":\"SubMoscow\"}]}]";
		mockHttpResponse(200, jsonResponse);

		JsonNode jsonNode = realObjectMapper.readTree(jsonResponse);
		when(objectMapper.readTree(jsonResponse)).thenReturn(jsonNode);

		// Act
		Integer areaId = areaUtil.getAreaIdFromApi(areaName);

		// Assert
		assertEquals(2, areaId);
	}

}
