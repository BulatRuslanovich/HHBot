package com.bipbup.utils;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

@Slf4j
@Component
public class AreaUtil {
    private static final String URL = "https://api.hh.ru/areas";
    private static final int HTTP_STATUS_OK = 200;

    private AreaUtil() {
    }

    public static String getAreaIdByName(final String areaName) {
        var client = HttpClient.newHttpClient();
        var uri = URI.create(URL);
        var request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response;

        try {
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

    private static String findAreaId(final JSONArray areas,
                                     final String name) {
        for (int i = 0; i < areas.length(); i++) {
            var area = areas.getJSONObject(i);

            var areaName = area.getString("name");
            if (areaName.equalsIgnoreCase(name)) {
                return area.getString("id");
            }

            if (area.has("areas")) {
                var foundAreaId = findAreaId(area.getJSONArray("areas"), name);
                if (foundAreaId != null)
                    return foundAreaId;

            }
        }

        return null;
    }
}
