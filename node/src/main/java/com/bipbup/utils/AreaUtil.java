package com.bipbup.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Slf4j
@Component
@RequiredArgsConstructor
public class AreaUtil {

    private static final String URL = "https://api.hh.ru/areas";

    private final HttpClient client;

    public Integer getAreaIdFromApi(String areaName) {
        if (areaName == null)
            return null;

        var uri = URI.create(URL);
        var request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Error querying API", e);
            return null;
        }

        if (response.statusCode() == HttpStatus.OK.value()) {
            var areas = new JSONArray(response.body());
            var areaId = findAreaId(areas, areaName);

            if (areaId == null)
                return null;

            return Integer.valueOf(areaId);
        } else {
            log.error("Error querying API {}: {} {}",
                    response.request().method(),
                    response.request().uri(),
                    response.statusCode());

            return null;
        }
    }

    private String findAreaId(final JSONArray areas, final String name) {
        Deque<JSONArray> deque = new ArrayDeque<>();
        deque.push(areas);

        while (!deque.isEmpty()) {
            var currentAreas = deque.pop();
            for (int i = 0; i < currentAreas.length(); i++) {
                var area = currentAreas.getJSONObject(i);

                var areaName = area.getString("name");
                if (name.equalsIgnoreCase(areaName))
                    return area.getString("id");

                if (area.has("areas"))
                    deque.push(area.getJSONArray("areas"));
            }
        }

        return null;
    }
}
