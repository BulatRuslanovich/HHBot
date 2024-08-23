package com.bipbup.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Stack;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Slf4j
@UtilityClass
public class AreaUtil {

    private final String URL = "https://api.hh.ru/areas";
    private final int HTTP_STATUS_OK = 200;

    public String getAreaIdFromApi(final String areaName) {
        if (areaName == null)
            return null;

        var uri = URI.create(URL);
        var request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response;

        try (var client = HttpClient.newHttpClient()) {
            response = client.send(request, ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Error querying API", e);
            return null;
        }

        if (response.statusCode() == HTTP_STATUS_OK) {
            var areas = new JSONArray(response.body());
            return findAreaId(areas, areaName);
        } else {
            log.error("Error querying API {}: {} {}",
                    response.request().method(),
                    response.request().uri(),
                    response.statusCode());

            return null;
        }
    }

    private String findAreaId(final JSONArray areas, final String name) {
        Stack<JSONArray> stack = new Stack<>();
        stack.push(areas);

        while (!stack.isEmpty()) {
            var currentAreas = stack.pop();
            for (int i = 0; i < currentAreas.length(); i++) {
                var area = currentAreas.getJSONObject(i);

                var areaName = area.getString("name");
                if (name.equalsIgnoreCase(areaName))
                    return area.getString("id");

                if (area.has("areas"))
                    stack.push(area.getJSONArray("areas"));
            }
        }

        return null;
    }
}
